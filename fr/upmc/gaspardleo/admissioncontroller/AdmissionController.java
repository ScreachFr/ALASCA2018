package fr.upmc.gaspardleo.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableOutboundPort;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherOutboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;
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
	private Map<Map<RGPortTypes, String>, Map<RDPortTypes, String>> requestSources;
	// Map<RD_URI, AVM> XXX Peut-être pas utile (gardé pour unregister). 
	private Map<String, Map<ApplicationVMPortTypes, String>> registeredAVMs;
	private Map<ComputerPoolPorts, String> computerPoolURIs;
	private ComputerPoolOutboundPort cpop;
	private ComponentCreator cc;
	
	public AdmissionController(
			HashMap<ComputerPoolPorts, String> computerPoolUri,
			String admissionController_IN,
			ComponentCreator cc) throws Exception{		
		
		super(1, 1);

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

		this.cpop.doConnection(
				computerPoolURIs.get(ComputerPoolPorts.COMPUTER_POOL), 
				ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());

		this.cc = cc;
		
		this.toggleLogging();
		//this.toggleTracing();
	}

	@Override
	public void addRequestDispatcher(
			HashMap<RDPortTypes, String> RD_uris,
			HashMap<RGPortTypes, String> RG_uris) throws Exception {
		
		String rd_URI = RD_uris.get(RDPortTypes.INTROSPECTION);

		RequestDispatcherOutboundPort rdop = new RequestDispatcherOutboundPort(this);
		this.addPort(rdop);
		rdop.publishPort();

		rdop.doConnection(
				RD_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN), 
				ClassFactory.newConnector(RequestDispatcherI.class).getCanonicalName());

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
				avm0_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
				currentNotifPortUri);

		currentNotifPortUri = rdop.registerVM(
				avm2_URIs,
				RequestSubmissionI.class);
		
		doAVMRequestNotificationConnection(
				avm0_URIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
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
		
		avmcop.doConnection(
			AVMConnectionPort_URI, 
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

		// TODO Attendre la fin du shutdown avant de faire ça ?
		//sop.doDisconnection();
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() {

		return this.avmPorts;
	}

	public static HashMap<ACPortTypes, String> newInstance(
			Map<ComputerPoolPorts, String> computerPoolUri,
			ComponentCreator cc) throws Exception{

		String admissionController_IN = AbstractPort.generatePortURI();

		Object[] constructorParams = new Object[] {
				computerPoolUri,
				admissionController_IN,
				cc
		};

		try {
			cc.createComponent(AdmissionController.class, constructorParams);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		HashMap<ACPortTypes, String> ret = new HashMap<ACPortTypes, String>();		
		ret.put(ACPortTypes.ADMISSION_CONTROLLER_IN, admissionController_IN);

		return ret;		
	}
}