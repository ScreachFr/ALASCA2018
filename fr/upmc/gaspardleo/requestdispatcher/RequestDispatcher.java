package fr.upmc.gaspardleo.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
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
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestNotificationHandlerInboundPort;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestNotificationHandlerOutboundPort;

public class RequestDispatcher 
extends AbstractComponent 
implements RequestDispatcherI, RequestSubmissionHandlerI , RequestNotificationHandlerI, RequestNotificationI {

	public static enum	RDPortTypes {
		REQUEST_SUBMISSION_IN, REQUEST_NOTIFICATION_OUT, INTROSPECTION, REQUEST_NOTIFICATION_HANDLER_IN, REQUEST_NOTIFICATION_HANDLER_OUT
	}
	
	private String 										dispatcherUri;
	
	// VMs
	private ArrayList<String> 							registeredVmsUri;
	private ArrayList<RequestSubmissionOutboundPort> 	registeredVmsRsop;
	private ArrayList<RequestNotificationInboundPort> 	registeredVmsRnip;
	
	//Ports
	private RequestSubmissionInboundPort 				rsip;
	private RequestNotificationOutboundPort 			rnop;
	private RequestSubmissionOutboundPort 				rsop;
	private RequestNotificationInboundPort 				rnip;
	private RequestNotificationHandlerOutboundPort 		rnhop;
	private RequestNotificationHandlerInboundPort 		rnhip;
	
	//Misc
	private Integer 									vmCursor;

	public RequestDispatcher(String dispatcherUri, 
			String RG_RequestNotificationInboundPortURI, String RG_RequestNotificationHandlerInboundPortURI) throws Exception {
		super(1, 1);

		this.dispatcherUri 		= dispatcherUri;
		this.registeredVmsUri 	= new ArrayList<>();
		this.registeredVmsRsop 	= new ArrayList<>();
		this.registeredVmsRnip	= new ArrayList<>();
		this.vmCursor 			= 0;

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
		
		
		// Request notification Handler
		this.rnhop = new RequestNotificationHandlerOutboundPort(this);
		this.addPort(this.rnhop);
		this.rnhop.publishPort();
		
		this.rnhip = new RequestNotificationHandlerInboundPort(this);
		this.addPort(rnhip);
		this.rnhip.publishPort();
		this.addRequiredInterface(RequestNotificationHandlerI.class);
		
		
		rnop.doConnection(RG_RequestNotificationInboundPortURI, RequestNotificationConnector.class.getCanonicalName());
		rnhop.doConnection(RG_RequestNotificationHandlerInboundPortURI, 
				ClassFactory.newConnector(RequestNotificationHandlerI.class).getCanonicalName());
		System.out.println("rnop is connected ? " + rnop.connected());
		System.out.println("rnhop is connected ? " + rnhop.connected());
		
	}

	@Override
	public String registerVM(String vmUri, String VM_requestSubmissionInboundPort) throws Exception {
	
		if (this.registeredVmsUri.contains(vmUri)) 
			return null; // TODO retourner l'uri qui existe déjà.

		RequestSubmissionOutboundPort rsop = new RequestSubmissionOutboundPort(this);
		this.addPort(rsop);
		rsop.publishPort();
		
		rsop.doConnection(
				VM_requestSubmissionInboundPort, 
				RequestSubmissionConnector.class.getCanonicalName());
		
		RequestNotificationInboundPort rnip = new RequestNotificationInboundPort(this);
		this.addPort(rnip);
		rnip.publishPort();
		
		
		this.registeredVmsRnip.add(rnip);
		this.registeredVmsRsop.add(rsop);
		this.registeredVmsUri.add(this.registeredVmsUri.size(), vmUri);
		
		this.logMessage(this.dispatcherUri + " : " + vmUri + " has been added.");
		
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
		
		this.logMessage(this.dispatcherUri + " : incoming request submission");
		
		if (this.registeredVmsRsop.size() == 0) {
			this.logMessage(this.dispatcherUri + " : no registered vm.");
			
		} else {
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(
					vmCursor%this.registeredVmsRsop.size()); 

			if (!rsop.connected()) {
				throw new Exception(this.dispatcherUri + " can't conect to vm.");
			}

			rsop.submitRequest(r);

			vmCursor++;
		}

	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		this.logMessage(this.dispatcherUri + " : incoming request submission and notification.");

		if (this.registeredVmsRsop.size() == 0) {
			this.logMessage(this.dispatcherUri + " : no registered vm.");
		} else {
			vmCursor = (vmCursor+1)%this.registeredVmsRsop.size();
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(vmCursor); 
			this.logMessage(this.dispatcherUri + " is using " + this.registeredVmsUri.get(vmCursor));
			
			if (!rsop.connected()) {
				throw new Exception(this.dispatcherUri + " can't conect to vm.");
			}
			
			rsop.submitRequestAndNotify(r);
		}
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		this.logMessage(this.dispatcherUri + " : incoming request termination notification.");

		rnop.notifyRequestTermination(r);
		rnhop.acceptRequestTerminationNotification(r);
	}
	
	@Override
	public void notifyRequestTermination(RequestI r) throws Exception {
		this.logMessage(this.dispatcherUri + " : incoming request termination notification.");
		
		rnhop.acceptRequestTerminationNotification(r);
		rnop.notifyRequestTermination(r);
	}
	
	public Map<RDPortTypes, String>	getRDPortsURI() throws Exception {
		HashMap<RDPortTypes, String> ret =
				new HashMap<RDPortTypes, String>() ;		
		ret.put(RDPortTypes.REQUEST_SUBMISSION_IN,
				this.rsip.getPortURI()) ;
		ret.put(RDPortTypes.REQUEST_NOTIFICATION_OUT,
				this.rnop.getPortURI()) ;
		ret.put(RDPortTypes.INTROSPECTION,
				this.dispatcherUri);
		ret.put(RDPortTypes.REQUEST_NOTIFICATION_HANDLER_IN,
				this.rnhip.getPortURI());
		ret.put(RDPortTypes.REQUEST_NOTIFICATION_HANDLER_OUT,
				this.rnhop.getPortURI());
		return ret ;
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

	
}
