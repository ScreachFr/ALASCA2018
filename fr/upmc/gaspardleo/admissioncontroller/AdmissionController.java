package fr.upmc.gaspardleo.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableOutboundPort;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherOutboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerInboundPort;
import fr.upmc.gaspardleo.requestdispatcher.connectors.RequestDispatherConnector;

public class AdmissionController 
extends AbstractComponent
implements AdmissionControllerI{

	private final static int DEFAULT_CORE_NUMBER = 1;

	public static enum	ACPortTypes {
		INTROSPECTION,
		ADMISSION_CONTROLLER_IN;
	}

	private DynamicComponentCreationOutboundPort dcc;
	private AdmissionControllerInboundPort acip;
	private ArrayList<ApplicationVMManagementOutboundPort> avmPorts;

	// Map<RequestGenerator, RequestDispatcher>
	private Map<Map<RGPortTypes, String>, Map<RDPortTypes, String>> requestSources;
	// Map<RD_URI, AVM> XXX Peut-être pas utile (gardé pour unregister). 
	private Map<String, Map<ApplicationVMPortTypes, String>> registeredAVMs;

	private Map<ComputerPoolPorts, String> computerPoolURIs;
	private ComputerPoolOutboundPort cpop;

	public AdmissionController(String AC_URI,
			HashMap<ComputerPoolPorts, String> computerPoolUri,
			String admissionController_IN,
			DynamicComponentCreationOutboundPort dcc) throws Exception{		
		super(1, 1);

		this.dcc = dcc;
		this.registeredAVMs = new HashMap<>();
		this.requestSources = new HashMap<>();


		this.avmPorts 	= new ArrayList<ApplicationVMManagementOutboundPort>();
		this.computerPoolURIs = computerPoolUri;


		this.addOfferedInterface(AdmissionControllerI.class);
		this.acip = new AdmissionControllerInboundPort(admissionController_IN, this);
		this.addPort(this.acip);
		this.acip.publishPort();		

		this.addRequiredInterface(ComputerPoolI.class);
		this.cpop = new ComputerPoolOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(cpop);
		this.cpop.publishPort();

		this.cpop.doConnection(computerPoolURIs.get(ComputerPoolPorts.COMPUTER_POOL), 
				ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());


		this.toggleLogging();
		this.toggleTracing();
	}

	@Override
	public void addRequestDispatcher(
			String RD_Component_URI,
			Map<RGPortTypes, String> requestGeneratorURIs
			) throws Exception {

		this.logMessage("Admission controller : adding a request source...");
		Map<RDPortTypes, String> RD_uris = RequestDispatcher.newInstance(dcc,
				RD_Component_URI,
				requestGeneratorURIs.get(RGPortTypes.REQUEST_NOTIFICATION_IN),
				requestGeneratorURIs.get(RGPortTypes.REQUEST_SUBMISSION_OUT),
				requestGeneratorURIs.get(RGPortTypes.CONNECTION_IN));
		
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

		// RNIP
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
		doAVMRequestNotificationConnection(avm1_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				currentNotifPortUri);
		this.registeredAVMs.put(rd_URI, avm1_URIs);

		currentNotifPortUri = rdop.registerVM(
				avm2_URIs,
				RequestSubmissionI.class);
		doAVMRequestNotificationConnection(avm2_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				currentNotifPortUri);
		this.registeredAVMs.put(rd_URI, avm2_URIs);
		this.logMessage("Admission controller : Request source successfully added!");
	}

	private void doAVMRequestNotificationConnection(String AVMConnectionPort_URI,
			String notificationPort_URI) throws Exception {
		this.logMessage("Admission controller : connection on notification port.");
		ApplicationVMConnectionOutboundPort avmcop 
				= new ApplicationVMConnectionOutboundPort(AbstractPort.generatePortURI(), this);

		this.addPort(avmcop);
		avmcop.publishPort();
 		avmcop.doConnection(AVMConnectionPort_URI, 
				ClassFactory.newConnector(ApplicationVMConnectionsI.class).getCanonicalName());
		

		avmcop.doRequestNotificationConnection(notificationPort_URI);
		
		this.logMessage("Admission controller : avmcop connection status : " + avmcop.connected());
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

	public static Map<ACPortTypes, String> newInstance(
			String AC_URI,
			Map<ComputerPoolPorts, String> computerPoolUri,
			DynamicComponentCreationOutboundPort dcc) throws Exception{

		String admissionController_IN = AbstractPort.generatePortURI();

		Object[] args = new Object[] {
				AC_URI,
				computerPoolUri,
				admissionController_IN,
				dcc
		};

		try {
			dcc.createComponent(AdmissionController.class.getCanonicalName(), args);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		HashMap<ACPortTypes, String> ret = new HashMap<ACPortTypes, String>();		
		ret.put(ACPortTypes.INTROSPECTION, AC_URI);
		ret.put(ACPortTypes.ADMISSION_CONTROLLER_IN, admissionController_IN);

		return ret;		
	}
}