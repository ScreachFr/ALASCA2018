package fr.upmc.gaspardleo.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreator;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableOutboundPort;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPooOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherOutboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.requestdispatcher.connectors.RequestDispatherConnector;

public class AdmissionController 
		extends AbstractComponent
		implements AdmissionControllerI{

	private final static int DEFAULT_CORE_NUMBER = 2;
	
	public static enum	ACPortTypes {
		INTROSPECTION
	}

	private DynamicComponentCreator dcc;
	private AdmissionControllerOutboundPort acop;
	private ArrayList<ApplicationVMManagementOutboundPort> avmPorts;
	
	// Map<RequestGenerator, RequestDispatcher>
	private Map<Map<RGPortTypes, String>, Map<RDPortTypes, String>> requestSources;
	// Map<RD_URI, AVM> XXX Peut-être pas utile (gardé pour unregister). 
	private Map<String, Map<ApplicationVMPortTypes, String>> registeredAVMs;
	
	private Map<ComputerPoolPorts, String> computerPoolURIs;
	private ComputerPooOutboundPort cpop;
	
	public AdmissionController(String AC_URI, Map<ComputerPoolPorts, String> computerPoolUri, DynamicComponentCreator dcc) throws Exception{		
		super(1, 1);
		
		this.avmPorts 	= new ArrayList<ApplicationVMManagementOutboundPort>();
		this.computerPoolURIs = computerPoolUri;
		
		
		this.addOfferedInterface(AdmissionControllerI.class);
		this.acop = new AdmissionControllerOutboundPort(AC_URI, this);
		this.addPort(this.acop);
		this.acop.publishPort();		
		
		this.addRequiredInterface(ComputerPoolI.class);
		this.cpop = new ComputerPooOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(cpop);
		this.cpop.publishPort();
		
		this.cpop.doConnection(computerPoolURIs.get(ComputerPoolPorts.COMPUTER_POOL), 
				ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());
		
		
		this.toggleLogging();
		this.toggleTracing();
	}
	
	public AdmissionControllerOutboundPort getProtToConnectWithRG(){
		return acop;
	}
	
	@Override
	public void addRequestDispatcher(
			String RD_Component_URI,
			Map<RGPortTypes, String> requestGeneratorURIs
			) throws Exception {
		
		String RG_RequestNotificationInboundPortURI = requestGeneratorURIs.get(RGPortTypes.REQUEST_NOTIFICATION_IN);
		
		Map<RDPortTypes, String> RD_uris = RequestDispatcher.newInstance(dcc, RD_Component_URI, RG_RequestNotificationInboundPortURI);
		String rd_URI = RD_uris.get(RDPortTypes.INTROSPECTION);
		
		
		RequestDispatcherOutboundPort rdop = new RequestDispatcherOutboundPort(this);
		this.addPort(rdop);
		rdop.publishPort();
		
		rdop.doConnection(
				RD_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN), 
				RequestDispatherConnector.class.getCanonicalName());
		
		// Vm applications creation
		
		Map<ApplicationVMPortTypes, String> avm0_URIs = 
				cpop.createNewApplicationVM("avm-" + RD_Component_URI + "-0", DEFAULT_CORE_NUMBER);
		Map<ApplicationVMPortTypes, String> avm1_URIs = 
				cpop.createNewApplicationVM("avm-" + RD_Component_URI + "-1", DEFAULT_CORE_NUMBER);
		Map<ApplicationVMPortTypes, String> avm2_URIs = 
				cpop.createNewApplicationVM("avm-" + RD_Component_URI + "-2", DEFAULT_CORE_NUMBER);
		
		
		
		String currentNotifPortUri;
		
		// Register application VM in Request Dispatcher
		currentNotifPortUri = rdop.registerVM(
				avm0_URIs,
				RequestSubmissionI.class);
		doAVMRequestNotificationConnection(avm0_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				currentNotifPortUri);
		this.registeredAVMs.put(rd_URI, avm0_URIs);
		
		currentNotifPortUri = rdop.registerVM(
				avm1_URIs,
				RequestSubmissionI.class);
		doAVMRequestNotificationConnection(avm0_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				currentNotifPortUri);
		this.registeredAVMs.put(rd_URI, avm1_URIs);
		
		currentNotifPortUri = rdop.registerVM(
				avm2_URIs,
				RequestSubmissionI.class);
		doAVMRequestNotificationConnection(avm0_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				currentNotifPortUri);
		this.registeredAVMs.put(rd_URI, avm2_URIs);
		
	}
	
	private void doAVMRequestNotificationConnection(String AVMConnectionPort_URI,
			String notificationPort_URI) throws Exception {
		ApplicationVMConnectionOutboundPort avmcop 
			= new ApplicationVMConnectionOutboundPort(AbstractPort.generatePortURI(), this);
		
		this.addPort(avmcop);
		avmcop.publishPort();
		avmcop.doConnection(AVMConnectionPort_URI, 
				ClassFactory.newConnector(ApplicationVMConnectionsI.class).getCanonicalName());
		
		
		avmcop.doRequestNotificationConnection(notificationPort_URI);
	}
	
	
	@Override
	public void removeRequestSource(String requestGeneratorURI) throws Exception {
		Optional<Map<RGPortTypes,String>> optRD = 
				requestSources.keySet().stream()
				.filter((e) -> e.get(RGPortTypes.INTROSPECTION).equals(requestGeneratorURI))
				.findFirst();
		
		if (!optRD.isPresent()) {
			this.logMessage("Remove request source : Can't find the request generator you're looking for!");
			return;
		}
		
		ShutdownableOutboundPort sop = new ShutdownableOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(sop);
		sop.publishPort();
		
		sop.shutdown();
		
		requestSources.remove(optRD.get());
		
		// XXX Attendre la fin du shutdown avant de faire ça ?
		//sop.doDisconnection();
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() {

		return this.avmPorts;
	}
	
	public static Map<ACPortTypes, String> newInstance(DynamicComponentCreator dcc, String AC_URI) throws Exception{

		dcc.createComponent(AdmissionController.class.getCanonicalName(), new Object[]{AC_URI, dcc});
		
		HashMap<ACPortTypes, String> ret = new HashMap<ACPortTypes, String>();		
		ret.put(ACPortTypes.INTROSPECTION, AC_URI);
		
		return ret;		
	}
}