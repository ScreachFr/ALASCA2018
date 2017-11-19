package fr.upmc.gaspardleo.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;

public class AdmissionController 
		extends AbstractComponent
		implements AdmissionControllerI{

	public static enum	ACPortTypes {
		INTROSPECTION
	}

	// Uri
	private String											uri;

	// Ports
//	private AdmissionControllerOutboundPort 				acop;
	
	// RequestSource related components
	private Map<String, RequestDispatcher>					RDs;
	private Map<String, List<ApplicationVM>>				AVMs;
	private ArrayList<ApplicationVMManagementOutboundPort> 	avmPorts;
	
	public AdmissionController(String ac_uri) throws Exception{
		
		super(1, 1);

		this.uri = ac_uri;
		
		this.RDs 		= new HashMap<String, RequestDispatcher>();
		this.AVMs 		= new HashMap<>();
		this.avmPorts 	= new ArrayList<ApplicationVMManagementOutboundPort>();
		
		this.toggleLogging();
		this.toggleTracing();
	}
	
	@Override
	public RequestDispatcher addRequestDispatcher(
			String RD_URI,
			String RG_RequestNotificationInboundPortURI, String RG_RequestNotificationHandlerInboundPortURI) throws Exception {
		
		// Request Dispatcher creation
		RequestDispatcher rd = new RequestDispatcher(RD_URI, 
				RG_RequestNotificationInboundPortURI,
				RG_RequestNotificationHandlerInboundPortURI);
		
		// Request Dispatcher debug
		rd.toggleLogging();
		rd.toggleTracing();
		
		String result = rd.getRDPortsURI().get(RDPortTypes.REQUEST_SUBMISSION_IN);
		
		RDs.put(result, rd);

		return rd;
	}
	
	@Override
	public ArrayList<ApplicationVM> addApplicationVMs(RequestDispatcher rd) throws Exception {
		
		String numRD = rd.getRDPortsURI().get(RDPortTypes.INTROSPECTION).split("-")[1];
		
		// Vm applications creation
		ApplicationVM vm0 = createApplicationVM("vm-" + numRD + "-0");
		ApplicationVM vm1 = createApplicationVM("vm-" + numRD + "-1");
		ApplicationVM vm2 = createApplicationVM("vm-" + numRD + "-2");
		
		ArrayList<ApplicationVM> newAVMs = new ArrayList<>();
		newAVMs.add(vm0);
		newAVMs.add(vm1);
		newAVMs.add(vm2);
		
		String currentNotifPortUri;
		
		// Register application VM in Request Dispatcher
		currentNotifPortUri = rd.registerVM(
				vm0.getNewAVMPortsURI().get(ApplicationVMPortTypes.INTROSPECTION),
				vm0.getNewAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));
		
		vm0.doRequestNotificationConnection(currentNotifPortUri);
		currentNotifPortUri = rd.registerVM(
				vm1.getNewAVMPortsURI().get(ApplicationVMPortTypes.INTROSPECTION),
				vm1.getNewAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));
		vm1.doRequestNotificationConnection(currentNotifPortUri);
		currentNotifPortUri = rd.registerVM(
				vm2.getNewAVMPortsURI().get(ApplicationVMPortTypes.INTROSPECTION),
				vm2.getNewAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));
		vm2.doRequestNotificationConnection(currentNotifPortUri);
				
		AVMs.put(rd.getRDPortsURI().get(RDPortTypes.REQUEST_SUBMISSION_IN), newAVMs);
		
		return newAVMs;
	}
	
	/**
	 * Créer une AVM.
	 * @param VM_URI
	 * 		Uri de la nouvelle AVM.
	 * @param cvm
	 * 		Utile pour l'allocation de core. TODO Créer un composant pour gerer les ordinateurs.
	 * @return
	 * 		L'AVM créée.
	 * @throws Exception
	 */
	private ApplicationVM createApplicationVM(String VM_URI) throws Exception{
				
		// Vm applications creation
		ApplicationVM vm = new ApplicationVM(VM_URI);
		
		// VM debug
		vm.toggleTracing();
		vm.toggleLogging();
				
		// Create a mock up port to manage the AVM component (allocate cores).
		ApplicationVMManagementOutboundPort avmPort = new ApplicationVMManagementOutboundPort(
				new AbstractComponent(0, 0) {});
		avmPort.publishPort();
		
		avmPort.doConnection(
				vm.getNewAVMPortsURI().get(ApplicationVMPortTypes.MANAGEMENT),
				ApplicationVMManagementConnector.class.getCanonicalName());
		
		this.avmPorts.add(avmPort);
				
		return vm;
	}
	
	//TODO delete RD avec unregisterVM
	@Override
	public void removeRequestSource(String RD_RequestSubmissionInboundPortUri) throws Exception {
		RDs.get(RD_RequestSubmissionInboundPortUri).shutdown();
		for (ApplicationVM vm :  AVMs.get(RD_RequestSubmissionInboundPortUri)) {
			vm.shutdown();
		}
		
		RDs.remove(RD_RequestSubmissionInboundPortUri);
		AVMs.remove(RD_RequestSubmissionInboundPortUri);
	}
	
	public Map<ACPortTypes, String>	getACPortsURI() throws Exception {
		HashMap<ACPortTypes, String> ret =
				new HashMap<ACPortTypes, String>();		
		ret.put(ACPortTypes.INTROSPECTION,
				this.uri);
		return ret ;
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() {

		return this.avmPorts;
	}
}