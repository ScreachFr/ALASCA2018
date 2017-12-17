package fr.upmc.gaspardleo.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreator;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableOutboundPort;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
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

	public static enum	ACPortTypes {
		INTROSPECTION
	}

	private DynamicComponentCreator dcc;
	private AdmissionControllerOutboundPort acop;
	private ArrayList<ApplicationVMManagementOutboundPort> avmPorts;
	
	// Map<RequestGenerator, RequestDispatcher>
	private Map<Map<RGPortTypes, String>, Map<RDPortTypes, String>> requestSources;
	
	
	public AdmissionController(String AC_URI, DynamicComponentCreator dcc) throws Exception{		
		super(1, 1);
		
		this.avmPorts 	= new ArrayList<ApplicationVMManagementOutboundPort>();
		
		this.addOfferedInterface(AdmissionControllerI.class);
		this.acop = new AdmissionControllerOutboundPort(AC_URI, this);
		this.addPort(this.acop);
		this.acop.publishPort();		
		
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
		
		RequestDispatcherOutboundPort rdop = new RequestDispatcherOutboundPort(this);
		this.addPort(rdop);
		rdop.publishPort();
		
		rdop.doConnection(
				RD_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN), 
				RequestDispatherConnector.class.getCanonicalName());
		
		// Vm applications creation
		ApplicationVM vm0 = createApplicationVM("vm-" + RD_Component_URI + "-0");
		ApplicationVM vm1 = createApplicationVM("vm-" + RD_Component_URI + "-1");
		ApplicationVM vm2 = createApplicationVM("vm-" + RD_Component_URI + "-2");
		
		ArrayList<ApplicationVM> newAVMs = new ArrayList<>();
		newAVMs.add(vm0);
		newAVMs.add(vm1);
		newAVMs.add(vm2);
		
		String currentNotifPortUri;
		
		// Register application VM in Request Dispatcher
		currentNotifPortUri = rdop.registerVM(
				vm0.getNewAVMPortsURI().get(ApplicationVMPortTypes.INTROSPECTION),
				vm0.getNewAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION),
				RequestSubmissionI.class);
		
		vm0.doRequestNotificationConnection(currentNotifPortUri);
		currentNotifPortUri = rdop.registerVM(
				vm1.getNewAVMPortsURI().get(ApplicationVMPortTypes.INTROSPECTION),
				vm1.getNewAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION),
				RequestSubmissionI.class);
		vm1.doRequestNotificationConnection(currentNotifPortUri);
		currentNotifPortUri = rdop.registerVM(
				vm2.getNewAVMPortsURI().get(ApplicationVMPortTypes.INTROSPECTION),
				vm2.getNewAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION),
				RequestSubmissionI.class);
		vm2.doRequestNotificationConnection(currentNotifPortUri);
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
	
	@Override
	public void removeRequestSource(String requestGeneratorURI) throws Exception {
		Map<RDPortTypes, String> rd = null;
		
		Optional<Map<RGPortTypes,String>> optRD = 
				requestSources.keySet().stream()
				.filter((e) -> e.get(RGPortTypes.INTROSPECTION).equals(requestGeneratorURI))
				.findFirst();
		
		if (!optRD.isPresent()) {
			this.logMessage("Remove request source : Can't find the request generator you're looking for!");
			return;
		}
		
		rd = requestSources.get(optRD.get());
		
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