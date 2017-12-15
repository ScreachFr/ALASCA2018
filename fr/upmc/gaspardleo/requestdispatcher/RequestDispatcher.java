package fr.upmc.gaspardleo.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreator;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherInboundPort;

public class RequestDispatcher 
extends AbstractComponent 
implements RequestDispatcherI, RequestSubmissionHandlerI , RequestNotificationHandlerI, RequestNotificationI {

	public static enum	RDPortTypes {
		REQUEST_SUBMISSION_IN, 
		REQUEST_SUBMISSION_OUT, 
		REQUEST_NOTIFICATION_OUT,
		REQUEST_NOTIFICATION_IN,
		REQUEST_DISPATCHER_IN,
		INTROSPECTION;
	}
	
	private String 										Component_URI;
	
	// VMs
	private ArrayList<String> 							registeredVmsUri;
	private ArrayList<RequestSubmissionOutboundPort> 	registeredVmsRsop;
	private ArrayList<RequestNotificationInboundPort> 	registeredVmsRnip;
	
	//Ports
	private RequestSubmissionInboundPort 				rsip;
	private RequestNotificationOutboundPort 			rnop;
	private RequestSubmissionOutboundPort 				rsop;
	private RequestNotificationInboundPort 				rnip;
	private RequestDispatcherInboundPort				rdip;
	//Misc
	private Integer 									vmCursor;

	public RequestDispatcher(
			String Component_URI, 
			String RG_RequestNotification_In,
			String RequestSubmission_In,
			String RequestSubmission_Out,
			String RequestNotification_In,
			String RequestNotification_Out) throws Exception {
		
		super(1, 1);

		this.Component_URI 		= Component_URI;
		this.registeredVmsUri 	= new ArrayList<>();
		this.registeredVmsRsop 	= new ArrayList<>();
		this.registeredVmsRnip	= new ArrayList<>();
		this.vmCursor 			= 0;

		//Request Dispatcher Inbound port connection.
		this.rdip = new RequestDispatcherInboundPort(this);
		this.addPort(rdip);
		this.rdip.publishPort();
		
		// Request submission inbound port connection.
		this.rsip = new RequestSubmissionInboundPort(this);
		this.addPort(this.rsip);
		this.rsip.publishPort();
		
		// Request submission outbound port connection.
        this.rsop = new RequestSubmissionOutboundPort(this) ;
		this.addPort(this.rsop);
		this.rsop.publishPort();
		this.addOfferedInterface(RequestSubmissionHandlerI.class) ;
				
		// Request notification submission inbound port connection.
		this.rnip = new RequestNotificationInboundPort(this);
		this.addPort(this.rnip);
		this.rnip.publishPort();
		
		// Request notification submission outbound port connection.
		this.rnop = new RequestNotificationOutboundPort(this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		this.addOfferedInterface(RequestNotificationI.class);
				
		this.rnop.doConnection(
				RG_RequestNotification_In, 
				RequestNotificationConnector.class.getCanonicalName());
		
		// Request Dispatcher debug
		this.toggleLogging();
		this.toggleTracing();			
	}

	@Override
	public String registerVM(String vmUri, String VM_requestSubmissionInboundPort, Class<?> vmInterface) throws Exception {
	
		if (this.registeredVmsUri.contains(vmUri)) 
			return null; // TODO retourner l'uri qui existe déjà.

		RequestSubmissionOutboundPort rsop = new RequestSubmissionOutboundPort(this);
		this.addPort(rsop);
		rsop.publishPort();
		
		rsop.doConnection(VM_requestSubmissionInboundPort, 
				ClassFactory.newConnector(vmInterface).getCanonicalName());
		
		RequestNotificationInboundPort rnip = new RequestNotificationInboundPort(this);
		this.addPort(rnip);
		rnip.publishPort();
		
		
		
		this.registeredVmsRnip.add(rnip);
		this.registeredVmsRsop.add(rsop);
		this.registeredVmsUri.add(this.registeredVmsUri.size(), vmUri);
		
		this.logMessage(this.Component_URI + " : " + vmUri + " has been added.");
		
		return rnip.getPortURI();
	}


	@Override
	public void unregisterVM(String vmUri) throws Exception {
		int index = registeredVmsUri.indexOf(vmUri);
		RequestSubmissionOutboundPort rsop = registeredVmsRsop.remove(index);
		registeredVmsUri.remove(index);
		rsop.doDisconnection();
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		
		this.logMessage(this.Component_URI + " : incoming request submission");
		
		if (this.registeredVmsRsop.size() == 0) {
			this.logMessage(this.Component_URI + " : no registered vm.");
			
		} else {
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(
					vmCursor%this.registeredVmsRsop.size()); 

			if (!rsop.connected()) {
				throw new Exception(this.Component_URI + " can't conect to vm.");
			}

			rsop.submitRequest(r);

			vmCursor++;
		}

	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		this.logMessage(this.Component_URI + " : incoming request submission and notification.");

		if (this.registeredVmsRsop.size() == 0) {
			this.logMessage(this.Component_URI + " : no registered vm.");
		} else {
			vmCursor = (vmCursor+1)%this.registeredVmsRsop.size();
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(vmCursor); 
			this.logMessage(this.Component_URI + " is using " + this.registeredVmsUri.get(vmCursor));
			
			if (!rsop.connected()) {
				throw new Exception(this.Component_URI + " can't conect to vm.");
			}
			
			rsop.submitRequestAndNotify(r);
		}
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		this.logMessage(this.Component_URI + " : incoming request termination notification.");

		rnop.notifyRequestTermination(r);
	}
	
	@Override
	public void notifyRequestTermination(RequestI r) throws Exception {
		this.logMessage(this.Component_URI + " : incoming request termination notification.");
		// XXX Pas utilisé.
		rnop.notifyRequestTermination(r);
	}
	
	@Override
	public void shutdown() throws ComponentShutdownException {
		for (RequestSubmissionOutboundPort rsop : registeredVmsRsop) {
			try {
				rsop.doDisconnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		registeredVmsUri.clear();
		registeredVmsRsop.clear();
		
		super.shutdown();
	}	
	
	public static Map<RDPortTypes, String> newInstance(
			DynamicComponentCreator dcc, 
			String Component_URI, 
			String RG_RequestNotification_In) throws Exception{
		
		String RequestSubmission_In = AbstractPort.generatePortURI();
		String RequestSubmission_Out = AbstractPort.generatePortURI();
		String RequestNotification_In = AbstractPort.generatePortURI();
		String RequestNotification_Out = AbstractPort.generatePortURI();
		String RequestDispatcher_In = AbstractPort.generatePortURI();
		
		Object[] args = new Object[]{ 
				Component_URI, 
				RG_RequestNotification_In,
				RequestSubmission_In,
				RequestSubmission_Out,
				RequestNotification_In,
				RequestNotification_Out,
				RequestDispatcher_In};
		
		dcc.createComponent(RequestDispatcher.class.getCanonicalName(), args);
		
		HashMap<RDPortTypes, String> ret = new HashMap<RDPortTypes, String>() ;		
		ret.put(RDPortTypes.INTROSPECTION, Component_URI);
		ret.put(RDPortTypes.REQUEST_SUBMISSION_IN, RequestSubmission_In);
		ret.put(RDPortTypes.REQUEST_SUBMISSION_OUT, RequestSubmission_Out);
		ret.put(RDPortTypes.REQUEST_NOTIFICATION_IN, RequestNotification_In);
		ret.put(RDPortTypes.REQUEST_NOTIFICATION_OUT, RequestNotification_Out);
		ret.put(RDPortTypes.REQUEST_DISPATCHER_IN, RequestDispatcher_In);
				
		return ret;
	}
}
