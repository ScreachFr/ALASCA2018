package fr.upmc.gaspardleo.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherInboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public 	class 		RequestDispatcher 
		extends 	AbstractComponent 
		implements 	RequestDispatcherI, 
					RequestSubmissionHandlerI , 
					RequestNotificationHandlerI, 
					RequestNotificationI, 
					ShutdownableI {

	public static enum	RDPortTypes {
		REQUEST_SUBMISSION_IN, 
		REQUEST_SUBMISSION_OUT, 
		REQUEST_NOTIFICATION_OUT,
		REQUEST_NOTIFICATION_IN,
		REQUEST_DISPATCHER_IN,
		INTROSPECTION,
		SHUTDOWNABLE_IN,
		REQUEST_GENERATOR_MANAGER_OUT,
		RQUEST_MONITOR_IN
	}
	
	private String Component_URI;
	private HashMap<String, RequestSubmissionOutboundPort> registeredVmsRsop;
	private HashMap<String, RequestNotificationInboundPort> registeredVmsRnip;
	private ArrayList<HashMap<ApplicationVMPortTypes, String>> registeredVmsUri;
	private RequestSubmissionInboundPort rsip;
	private String rnop_uri;
	private HashMap<RGPortTypes, String> rg_uris;
	private RequestNotificationInboundPort rnip;
	private RequestDispatcherInboundPort rdip;
	private ShutdownableInboundPort sip;
	private AdmissionControllerOutboundPort acop;
	private Integer vmCursor;
	private String rmop_uri;
	
	public RequestDispatcher(
		 	HashMap<RDPortTypes, String> component_uris, 
			HashMap<RGPortTypes, String> rg_uris,
			HashMap<ACPortTypes, String> ac_uris) throws Exception {
		
		super(1, 1);
			
		this.Component_URI = component_uris.get(RDPortTypes.INTROSPECTION);
		this.registeredVmsUri = new ArrayList<>();
		this.registeredVmsRsop = new HashMap<>();
		this.registeredVmsRnip = new HashMap<>();
		this.vmCursor = 0;

		// Request submission inbound port connection.
		this.addOfferedInterface(RequestSubmissionI.class);
		this.rsip = new RequestSubmissionInboundPort(
			component_uris.get(RDPortTypes.REQUEST_SUBMISSION_IN), 
			this);
		this.addPort(this.rsip);
		this.rsip.publishPort();
		// Request notification submission inbound port connection.
		this.addOfferedInterface(RequestNotificationI.class);
		this.rnip = new RequestNotificationInboundPort(
			component_uris.get(RDPortTypes.REQUEST_NOTIFICATION_IN), 
			this);
		this.addPort(this.rnip);
		this.rnip.publishPort();
		this.rnop_uri = component_uris.get(RDPortTypes.REQUEST_NOTIFICATION_OUT);
		this.rg_uris = rg_uris;
		assert component_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN) != null : "assertion : rg_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN) null";
		//RequestDispatcher
		this.addOfferedInterface(RequestDispatcherI.class);
		this.rdip = new RequestDispatcherInboundPort(
				component_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN), 
				this);	
		assert this.rdip != null : "assertion : this.rdip null";
		this.addPort(rdip);
		this.rdip.publishPort();
		// Shutdown port
		this.addOfferedInterface(ShutdownableI.class);
		this.sip = new ShutdownableInboundPort(
			component_uris.get(RDPortTypes.SHUTDOWNABLE_IN), 
			this);
		this.addPort(this.sip);
		this.sip.publishPort();
		//Admission Crontroler port
		this.addRequiredInterface(AdmissionControllerI.class);
		this.acop = new AdmissionControllerOutboundPort(this);
		this.acop.publishPort();
		this.addPort(acop);
		this.acop.doConnection(
				ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), 
				ClassFactory.newConnector(AdmissionControllerI.class).getCanonicalName());
		this.rmop_uri = component_uris.get(RDPortTypes.RQUEST_MONITOR_IN);
		 //Addition by AC the new RD for a specific RG
		this.acop.addRequestDispatcher(component_uris, rg_uris, this.rmop_uri);
		// Request Dispatcher debug
		this.toggleLogging();
		this.toggleTracing();
		this.logMessage("RequestDispatcher made");
	}
	
	private synchronized String getNextVmUriFromCursor() {
		return registeredVmsUri.get(vmCursor++%registeredVmsUri.size())
				.get(ApplicationVMPortTypes.INTROSPECTION);
	}

	@Override
	public String registerVM(
			HashMap<ApplicationVMPortTypes, String> avmURIs, 
			Class<?> vmInterface) throws Exception {
		this.logMessage("Register avm : " + avmURIs + "...");
		String avmUri = avmURIs.get(ApplicationVMPortTypes.INTROSPECTION);
		// Verifi si l'AVM est déjà registered.
		if (this.registeredVmsUri.stream().anyMatch((e) -> e.get(ApplicationVMPortTypes.INTROSPECTION).equals(avmUri))) { 
			this.logMessage("Register AVM : You just tried to register an AVM that already was registered it this RequestDispatcher.");
			return null;
		}
		RequestSubmissionOutboundPort rsop = new RequestSubmissionOutboundPort(this);
		this.addPort(rsop);
		rsop.publishPort();
//		rsop.doConnection(avmURIs.get(ApplicationVMPortTypes.REQUEST_SUBMISSION), 
//				ClassFactory.newConnector(vmInterface).getCanonicalName());
		rsop.doConnection(avmURIs.get(ApplicationVMPortTypes.REQUEST_SUBMISSION), 
				RequestSubmissionConnector.class.getCanonicalName());
		RequestNotificationInboundPort rnip = new RequestNotificationInboundPort(this);
		this.addPort(rnip);
		rnip.publishPort();
		this.registeredVmsRnip.put(avmUri, rnip);
		doAVMRequestNotificationAndMonitoringConnection(avmURIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				this.rnip.getPortURI(), this.rmop_uri);
		this.registeredVmsRsop.put(avmUri, rsop);
		this.registeredVmsUri.add(avmURIs);
		this.logMessage(this.Component_URI + " : " + avmURIs + " has been added.");
		return rnip.getPortURI();
	}

	@Override
	public void unregisterVM(String vmUri) throws Exception {
		Optional<HashMap<ApplicationVMPortTypes,String>> URIs = 
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
			vmCursor = (vmCursor+1) % this.registeredVmsUri.size();
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
		System.out.println("acceptRequestTerminationNotification function");
		try {
			RequestNotificationOutboundPort rnop = 
					(RequestNotificationOutboundPort) this.findPortFromURI(this.rnop_uri);
			if (rnop == null){
				this.addRequiredInterface(RequestSubmissionI.class) ;
				rnop = new RequestNotificationOutboundPort(this.rnop_uri, this) ;
				this.addPort(rnop) ;
				rnop.publishPort() ;
				rnop.doConnection(
						rg_uris.get(RGPortTypes.REQUEST_NOTIFICATION_IN), 
						RequestNotificationConnector.class.getCanonicalName());
			}
			this.logMessage(this.Component_URI + " : incoming request termination notification.");
			rnop.notifyRequestTermination(r);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	@Override
	public void notifyRequestTermination(RequestI r) throws Exception {
		RequestNotificationOutboundPort rnop = 
				(RequestNotificationOutboundPort) this.findPortFromURI(this.rnop_uri);
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
		try {
			this.logMessage("Admission controller : connection on notification port.");
			ApplicationVMConnectionOutboundPort avmcop 
					= new ApplicationVMConnectionOutboundPort(AbstractPort.generatePortURI(), this);
			this.addPort(avmcop);
			avmcop.publishPort();
			try{
				avmcop.doConnection(AVMConnectionPort_URI, 
					ClassFactory.newConnector(ApplicationVMConnectionsI.class).getCanonicalName());
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}
			assert avmcop.connected() : "avmop not connected";
//			avmcop.doConnection(AVMConnectionPort_URI, 
//					ApplicationVMConnector.class.getCanonicalName());
			avmcop.doRequestNotificationConnection(notificationPort_URI);
			avmcop.doRequestMonitorConnection(requestMonitor_in);
			this.logMessage("Admission controller : avmcop connection status : " + avmcop.connected());
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public List<String> getRegisteredAVMUris() {
		return registeredVmsUri.stream()
		   .map((e) -> e.get(ApplicationVMPortTypes.INTROSPECTION))
		   .collect(Collectors.toList());
	}
	
	public static HashMap<RDPortTypes, String> makeUris(int num_rd){
		HashMap<RDPortTypes, String> requestDispatcher_uris = new HashMap<RDPortTypes, String>() ;		
		requestDispatcher_uris.put(RDPortTypes.INTROSPECTION, "rd-"+num_rd);
		requestDispatcher_uris.put(RDPortTypes.REQUEST_SUBMISSION_IN, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_SUBMISSION_OUT, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_NOTIFICATION_IN, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_NOTIFICATION_OUT, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_DISPATCHER_IN, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.SHUTDOWNABLE_IN, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_GENERATOR_MANAGER_OUT, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.RQUEST_MONITOR_IN, AbstractPort.generatePortURI());
		return requestDispatcher_uris;
	}
}
