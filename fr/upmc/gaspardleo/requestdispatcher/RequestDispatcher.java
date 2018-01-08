package fr.upmc.gaspardleo.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
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
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherInboundPort;
import fr.upmc.gaspardleo.requestgenerator.connectors.RequestGeneraterConnector;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;
import fr.upmc.gaspardleo.requestgenerator.ports.RequestGeneratorOutboundPort;

public class RequestDispatcher 
	extends AbstractComponent 
	implements 
		RequestDispatcherI, RequestSubmissionHandlerI,
		RequestNotificationHandlerI, RequestNotificationI, ShutdownableI {

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
	private ArrayList<Map<ApplicationVMPortTypes, String>> 	registeredVmsUri;
	
	
	//Ports
	private RequestSubmissionInboundPort 				rsip;
	private RequestNotificationOutboundPort 			rnop;
	private RequestSubmissionOutboundPort 				rsop;
	private RequestNotificationInboundPort 				rnip;
	private RequestDispatcherInboundPort				rdip;
	private ShutdownableInboundPort						sip;
	private RequestGeneratorOutboundPort				rgop;
	
	//Misc
	private Integer 									vmCursor;

	//Monitoring
	private String rmop_uri;
	
	public RequestDispatcher(
			String Component_URI, 
			String RG_RequestNotification_In,
			String RG_RequestSubmission_Out,
			String RequestSubmission_In,
			String RequestSubmission_Out,
			String RequestNotification_In,
			String RequestNotification_Out,
			String RequestDispatcher_In,
			String ShutDownable_In,
			String RG_Connection_In,
			String RequestMonitor_In
			) throws Exception {
		
		super(1, 1);

		this.Component_URI 		= Component_URI;
		this.registeredVmsUri 	= new ArrayList<>();
		this.registeredVmsRsop 	= new HashMap<>();
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
		
		this.rgop = new RequestGeneratorOutboundPort(this);
		this.addPort(this.rgop);
		this.rgop.publishPort();
		this.addRequiredInterface(RequestGeneratorConnectionI.class);
			
		try {
		rgop.doConnection(RG_Connection_In, RequestGeneraterConnector.class.getCanonicalName());
		rgop.doConnectionWithRD(RequestSubmission_In);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
		this.rmop_uri = RequestMonitor_In;
		
		// Request Dispatcher debug
		this.toggleLogging();
		this.toggleTracing();
	}
	
	private synchronized String getNextVmUriFromCursor() {
		return registeredVmsUri.get(vmCursor++%registeredVmsUri.size())
				.get(ApplicationVMPortTypes.INTROSPECTION);
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
		
		
		doAVMRequestNotificationAndMonitoringConnection(avmURIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				this.rnip.getPortURI(), this.rmop_uri);
		
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
		registeredVmsRsop.get(vmUri).doDisconnection();
		
	}
	
	@Override
	public void unregisterVM() throws Exception {
		unregisterVM(getNextVmUriFromCursor());
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		this.logMessage(this.Component_URI + " : incoming request submission");
		
		
		if (this.registeredVmsUri.size() == 0) {
			this.logMessage(this.Component_URI + " : no registered vm.");
			
		} else {
			String avmURI = getNextVmUriFromCursor();
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(
					avmURI); 

			if (!rsop.connected()) {
				throw new Exception(this.Component_URI + " can't conect to vm.");
			}

			rsop.submitRequest(r);

		}

	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		this.logMessage(this.Component_URI + " : incoming request submission and notification.");
		
		System.out.println("There's " + registeredVmsUri.size() + " registered AVMs.");
		
		if (this.registeredVmsUri.size() == 0) {
			this.logMessage(this.Component_URI + " : no registered vm.");
		} else {
			
			String avmURI = getNextVmUriFromCursor();
			
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
	
	private void doAVMRequestNotificationAndMonitoringConnection(String AVMConnectionPort_URI,
			String notificationPort_URI, String requestMonitor_in) throws Exception {
		this.logMessage("Admission controller : connection on notification port.");
		ApplicationVMConnectionOutboundPort avmcop 
				= new ApplicationVMConnectionOutboundPort(AbstractPort.generatePortURI(), this);

		this.addPort(avmcop);
		avmcop.publishPort();
 		avmcop.doConnection(AVMConnectionPort_URI, 
				ClassFactory.newConnector(ApplicationVMConnectionsI.class).getCanonicalName());
		

		avmcop.doRequestNotificationConnection(notificationPort_URI);
		avmcop.doRequestMonitorConnection(requestMonitor_in);
		
		this.logMessage("Admission controller : avmcop connection status : " + avmcop.connected());
	}
	
	public static HashMap<RDPortTypes, String> newInstance(
			DynamicComponentCreationOutboundPort dcc, 
			String Component_URI, 
			String RG_RequestNotification_In,
			String RG_RequestSubmission_Out,
			String RG_Connection_In,
			String RequestMonitor_In) throws Exception {
		
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
				Shutdownable_In,
				RG_Connection_In,
				RequestMonitor_In
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

	@Override
	public List<String> getRegisteredAVMUris() {
		return registeredVmsUri.stream()
							   .map((e) -> e.get(ApplicationVMPortTypes.INTROSPECTION))
							   .collect(Collectors.toList());
	}
}
