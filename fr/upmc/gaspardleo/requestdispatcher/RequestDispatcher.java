package fr.upmc.gaspardleo.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.ports.AbstractPort;
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
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherInboundPort;

public class RequestDispatcher 
extends AbstractComponent 
implements RequestDispatcherI, RequestSubmissionHandlerI , RequestNotificationHandlerI, RequestNotificationI, ShutdownableI {

	public static enum	RDPortTypes {
		REQUEST_SUBMISSION_IN, 
		REQUEST_SUBMISSION_OUT, 
		REQUEST_NOTIFICATION_OUT,
		REQUEST_NOTIFICATION_IN,
		REQUEST_DISPATCHER_IN,
		INTROSPECTION,
		SHUTDOWNABLE_IN;
	}
	
	private String 										Component_URI;
	
	// VMs
	private Map<String, RequestSubmissionOutboundPort> 		registeredVmsRsop;
	private Map<String, RequestNotificationInboundPort> 	registeredVmsRnip;
	private ArrayList<Map<ApplicationVMPortTypes, String>> 	registeredVmsUri;
	
	
	//Ports
	private RequestSubmissionInboundPort 				rsip;
	private RequestNotificationOutboundPort 			rnop;
	private RequestSubmissionOutboundPort 				rsop;
	private RequestNotificationInboundPort 				rnip;
	private RequestDispatcherInboundPort				rdip;
	private ShutdownableInboundPort						sip;
	
	
	//Misc
	private Integer 									vmCursor;

	public RequestDispatcher(
			String Component_URI, 
			String RG_RequestNotification_In,
			String RG_RequestSubmission_Out,
			String RequestSubmission_In,
			String RequestSubmission_Out,
			String RequestNotification_In,
			String RequestNotification_Out,
			String RequestDispatcher_In,
			String ShutDownable_In) throws Exception {
		
		super(1, 1);

		this.Component_URI 		= Component_URI;
		this.registeredVmsUri 	= new ArrayList<>();
		this.registeredVmsRsop 	= new HashMap<>();
		this.registeredVmsRnip	= new HashMap<>();
		this.vmCursor 			= 0;

		// Request submission inbound port connection.
		this.rsip = new RequestSubmissionInboundPort(RequestSubmission_In, this);
		this.addPort(this.rsip);
		this.rsip.publishPort();
		
		
		// Request submission outbound port connection.
        this.rsop = new RequestSubmissionOutboundPort(RequestSubmission_Out, this) ;
		this.addPort(this.rsop);
		this.rsop.publishPort();
		this.addOfferedInterface(RequestSubmissionHandlerI.class) ;
				
		// Request notification submission inbound port connection.
		this.rnip = new RequestNotificationInboundPort(RequestNotification_In, this);
		this.addPort(this.rnip);
		this.rnip.publishPort();
		
		// Request notification submission outbound port connection.
		this.rnop = new RequestNotificationOutboundPort(RequestNotification_Out, this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		this.addOfferedInterface(RequestNotificationI.class);
		this.addRequiredInterface(RequestNotificationI.class);
				
		this.rnop.doConnection(
				RG_RequestNotification_In, 
				RequestNotificationConnector.class.getCanonicalName());
		
		//RequestDispatcher
		this.rdip = new RequestDispatcherInboundPort(RequestDispatcher_In, this);
		this.addOfferedInterface(RequestDispatcherI.class);
		this.addPort(rdip);
		this.rdip.publishPort();
		
		// Shutdown port
		this.sip = new ShutdownableInboundPort(ShutDownable_In, this);
		this.addPort(this.sip);
		this.sip.publishPort();
		this.addOfferedInterface(ShutdownableI.class);
		
		
		// Request Dispatcher debug
		this.toggleLogging();
	}

	@Override
	public String registerVM(Map<ApplicationVMPortTypes, String> avmURIs, Class<?> vmInterface) throws Exception {
		String avmUri = avmURIs.get(ApplicationVMPortTypes.INTROSPECTION);
		
		// Verifi si l'AVM est déjà registered.
		if (this.registeredVmsUri.stream().anyMatch((e) -> e.get(ApplicationVMPortTypes.INTROSPECTION).equals(avmUri))) { 
			this.logMessage("Register AVM : You just tried to register an AVM that already was registered it this RequestDispatcher.");
			return null;
		}
		
		
		RequestSubmissionOutboundPort rsop = new RequestSubmissionOutboundPort(this);
		this.addPort(rsop);
		rsop.publishPort();
		
		rsop.doConnection(avmURIs.get(ApplicationVMPortTypes.REQUEST_SUBMISSION), 
				ClassFactory.newConnector(vmInterface).getCanonicalName());
		
		RequestNotificationInboundPort rnip = new RequestNotificationInboundPort(this);
		this.addPort(rnip);
		rnip.publishPort();
		
		
		
		this.registeredVmsRnip.put(avmUri, rnip);
		this.registeredVmsRsop.put(avmUri, rsop);
		this.registeredVmsUri.add(avmURIs);
		
		this.logMessage(this.Component_URI + " : " + avmURIs + " has been added.");
		
		return rnip.getPortURI();
	}


	@Override
	public void unregisterVM(String vmUri) throws Exception {
		Optional<Map<ApplicationVMPortTypes,String>> URIs = 
				registeredVmsUri.stream()
				.filter(e -> e.get(ApplicationVMPortTypes.INTROSPECTION).equals(vmUri))
				.findFirst();
		
		if (!URIs.isPresent()) {
			this.logMessage("Unregister AVM : This AVM is not registered!");
			return;
		}
		
		registeredVmsUri.remove(URIs.get());
		registeredVmsRnip.get(vmUri).doDisconnection();
		registeredVmsRsop.get(vmUri).doDisconnection();
		
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		System.out.println("XXXXXXXXXXXXXXXXx Request accept !");
		this.logMessage(this.Component_URI + " : incoming request submission");
		
		if (this.registeredVmsUri.size() == 0) {
			this.logMessage(this.Component_URI + " : no registered vm.");
			
		} else {
			String avmURI = registeredVmsUri
					.get(vmCursor%registeredVmsUri.size()).get(ApplicationVMPortTypes.INTROSPECTION);
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(
					avmURI); 

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

		if (this.registeredVmsUri.size() == 0) {
			this.logMessage(this.Component_URI + " : no registered vm.");
		} else {
			vmCursor = (vmCursor+1) % this.registeredVmsUri.size();
			
			String avmURI = registeredVmsUri
					.get(vmCursor).get(ApplicationVMPortTypes.INTROSPECTION);
			
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(avmURI);
			
			this.logMessage(this.Component_URI + " is using " + avmURI);
			
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
		registeredVmsUri.forEach(e -> {
			String avmUri = e.get(ApplicationVMPortTypes.INTROSPECTION);
			
			try {
				registeredVmsRnip.get(avmUri).doDisconnection();
				registeredVmsRsop.get(avmUri).doDisconnection();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		registeredVmsUri.clear();
		registeredVmsRsop.clear();
		registeredVmsUri.clear();
		
		super.shutdown();
	}	
	
	public static Map<RDPortTypes, String> newInstance(
			DynamicComponentCreationOutboundPort dcc, 
			String Component_URI, 
			String RG_RequestNotification_In,
			String RG_RequestSubmission_Out) throws Exception {
		
		String RequestSubmission_In = AbstractPort.generatePortURI();
		String RequestSubmission_Out = AbstractPort.generatePortURI();
		String RequestNotification_In = AbstractPort.generatePortURI();
		String RequestNotification_Out = AbstractPort.generatePortURI();
		String RequestDispatcher_In = AbstractPort.generatePortURI();
		String Shutdownable_In = AbstractPort.generatePortURI();
		
		
		Object[] args = new Object[]{ 
				Component_URI, 
				RG_RequestNotification_In,
				RG_RequestSubmission_Out,
				RequestSubmission_In,
				RequestSubmission_Out,
				RequestNotification_In,
				RequestNotification_Out,
				RequestDispatcher_In,
				Shutdownable_In
		};
		
		try {
			dcc.createComponent(RequestDispatcher.class.getCanonicalName(), args);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		HashMap<RDPortTypes, String> ret = new HashMap<RDPortTypes, String>() ;		
		ret.put(RDPortTypes.INTROSPECTION, Component_URI);
		ret.put(RDPortTypes.REQUEST_SUBMISSION_IN, RequestSubmission_In);
		ret.put(RDPortTypes.REQUEST_SUBMISSION_OUT, RequestSubmission_Out);
		ret.put(RDPortTypes.REQUEST_NOTIFICATION_IN, RequestNotification_In);
		ret.put(RDPortTypes.REQUEST_NOTIFICATION_OUT, RequestNotification_Out);
		ret.put(RDPortTypes.REQUEST_DISPATCHER_IN, RequestDispatcher_In);
		ret.put(RDPortTypes.SHUTDOWNABLE_IN, Shutdownable_In);
		
		return ret;
	}
}
