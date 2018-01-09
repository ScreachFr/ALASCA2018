package fr.upmc.gaspardleo.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.applicationvm.connectors.ApplicationVMConnector;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionOutboundPort;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableOutboundPort;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.connectors.ComputerPoolConnector;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.connectors.RequestDispatherConnector;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherOutboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;
import fr.upmc.gaspardleo.requestgenerator.connectors.RequestGeneraterConnector;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;
import fr.upmc.gaspardleo.requestgenerator.ports.RequestGeneratorOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerInboundPort;

public class AdmissionController 
		extends AbstractComponent
		implements AdmissionControllerI{

	public static enum	ACPortTypes {
		ADMISSION_CONTROLLER_IN;
	}
	
	private final static int DEFAULT_CORE_NUMBER = 2;
	private AdmissionControllerInboundPort acip;
	private ArrayList<ApplicationVMManagementOutboundPort> avmPorts;
	// Map<RequestGenerator, RequestDispatcher>
	private HashMap<HashMap<RGPortTypes, String>, HashMap<RDPortTypes, String>> requestSources;
	// HashMap<RD_URI, AVM> XXX Peut-être pas utile (gardé pour unregister). 
	private HashMap<String, HashMap<ApplicationVMPortTypes, String>> registeredAVMs;
	private HashMap<ComputerPoolPorts, String> computerPoolURIs;
	private ComputerPoolOutboundPort cpop;
	private ComponentCreator cc;
	
	public AdmissionController(
			HashMap<ComputerPoolPorts, String> computerPoolUri,
			HashMap<ACPortTypes, String> ac_uris,
			ComponentCreator cc) throws Exception{		
		
		super(1, 1);

		this.registeredAVMs = new HashMap<>();
		this.requestSources = new HashMap<>();

		this.avmPorts 	= new ArrayList<ApplicationVMManagementOutboundPort>();
		this.computerPoolURIs = computerPoolUri;

		this.addOfferedInterface(AdmissionControllerI.class);
		this.acip = new AdmissionControllerInboundPort(ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), this);
		this.addPort(this.acip);
		this.acip.publishPort();
		
		if(AbstractCVM.isDistributed){
			assert this.acip.isDistributedlyPublished() : "ADMISSION_CONTROLLER_IN isn't distributedly published";
			System.out.println("[DEBUG LEO] AdmissionControllerInboundPort uri : " + ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN));
			System.out.println("[DEBUG LEO] AdmissionControllerInboundPort is distributedly published ? -> " + this.acip.isDistributedlyPublished());
		}
		
		this.addRequiredInterface(ComputerPoolI.class);
		this.cpop = new ComputerPoolOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(cpop);
		this.cpop.publishPort();

//		this.cpop.doConnection(
//				computerPoolURIs.get(ComputerPoolPorts.COMPUTER_POOL), 
//				ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());		
		
		this.cpop.doConnection(
			computerPoolURIs.get(ComputerPoolPorts.COMPUTER_POOL), 
			ComputerPoolConnector.class.getCanonicalName());		

		
		this.cc = cc;
		
		this.toggleLogging();
		this.toggleTracing();
		
		this.logMessage("AdmissionController made");
	}

	@Override
	public void addRequestDispatcher(
			HashMap<RDPortTypes, String> RD_uris,
			HashMap<RGPortTypes, String> RG_uris) throws Exception {
		
		//Request Generator port
		this.addRequiredInterface(RequestGeneratorConnectionI.class);
		RequestGeneratorOutboundPort rgop = new RequestGeneratorOutboundPort(this);
		this.addPort(rgop);
		rgop.publishPort();
		
//		rgop.doConnection(
//				RG_uris.get(RGPortTypes.CONNECTION_IN), 
//				ClassFactory.newConnector(RequestGeneratorConnectionI.class).getCanonicalName());
		
		rgop.doConnection(
				RG_uris.get(RGPortTypes.CONNECTION_IN), 
				RequestGeneraterConnector.class.getCanonicalName());

		rgop.doConnectionWithRD(
				RD_uris.get(RDPortTypes.REQUEST_SUBMISSION_IN));
		
		String rd_URI = RD_uris.get(RDPortTypes.INTROSPECTION);

		RequestDispatcherOutboundPort rdop = new RequestDispatcherOutboundPort(this);
		this.addPort(rdop);
		rdop.publishPort();

//		rdop.doConnection(
//				RD_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN), 
//				ClassFactory.newConnector(RequestDispatcherI.class).getCanonicalName());

		rdop.doConnection(
				RD_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN), 
				RequestDispatherConnector.class.getCanonicalName());
		
		// Vm applications creation

		System.out.println("AC is creating some AVMs");
		
		HashMap<ApplicationVMPortTypes, String> avm0_URIs = 
				cpop.createNewApplicationVM("avm-" + rd_URI + "-0", DEFAULT_CORE_NUMBER, cc);
		System.out.println("AVM0 done.");
		
		HashMap<ApplicationVMPortTypes, String> avm1_URIs = 
				cpop.createNewApplicationVM("avm-" + rd_URI + "-1", DEFAULT_CORE_NUMBER, cc);
		System.out.println("AVM1 done.");
		
		HashMap<ApplicationVMPortTypes, String> avm2_URIs = 
				cpop.createNewApplicationVM("avm-" + rd_URI + "-2", DEFAULT_CORE_NUMBER, cc);
		System.out.println("AVM2 done.");

		System.out.println("AVMs has been successfully created!");

		// Register application VM in Request Dispatcher
		
		 String currentNotifPortUri = rdop.registerVM(
				avm0_URIs,
				RequestSubmissionI.class);
		
		doAVMRequestNotificationConnection(
				avm0_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				currentNotifPortUri);
		
		currentNotifPortUri = rdop.registerVM(
				avm1_URIs,
				RequestSubmissionI.class);
		
		doAVMRequestNotificationConnection(
				avm1_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				currentNotifPortUri);

		currentNotifPortUri = rdop.registerVM(
				avm2_URIs,
				RequestSubmissionI.class);
		
		doAVMRequestNotificationConnection(
				avm2_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				currentNotifPortUri);
		
		this.registeredAVMs.put(rd_URI, avm0_URIs);
		this.registeredAVMs.put(rd_URI, avm1_URIs);
		this.registeredAVMs.put(rd_URI, avm2_URIs);
		
		this.logMessage("Admission controller : Request source successfully added!");
	}

	private void doAVMRequestNotificationConnection(
			String AVMConnectionPort_URI,
			String notificationPort_URI) throws Exception {
		
		this.logMessage("Admission controller : connection on notification port.");
		ApplicationVMConnectionOutboundPort avmcop 
				= new ApplicationVMConnectionOutboundPort(AbstractPort.generatePortURI(), this);

		this.addPort(avmcop);
		avmcop.publishPort();
		
//		avmcop.doConnection(
//			AVMConnectionPort_URI, 
//			ClassFactory.newConnector(ApplicationVMConnectionsI.class).getCanonicalName());

		avmcop.doConnection(
				AVMConnectionPort_URI, 
				ApplicationVMConnector.class.getCanonicalName());

		avmcop.doRequestNotificationConnection(notificationPort_URI);
		
		this.logMessage("Admission controller : avmcop connection status : " + avmcop.connected());
	}
	
	@Override
	public void removeRequestSource(String requestGeneratorURI) throws Exception {
		
		Optional<HashMap<RGPortTypes,String>> optRD = 
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

		// TODO Attendre la fin du shutdown avant de faire ça ?
		//sop.doDisconnection();
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() {

		return this.avmPorts;
	}

	public static HashMap<ACPortTypes, String> newInstance(
			HashMap<ComputerPoolPorts, String> computerPoolUri,
			HashMap<ACPortTypes, String> ac_uris,
			ComponentCreator cc) throws Exception{
		
		Object[] constructorParams = new Object[] {
				computerPoolUri,
				ac_uris,
				cc
		};

		try {
			cc.createComponent(AdmissionController.class, constructorParams);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}

		return ac_uris;		
	}
}