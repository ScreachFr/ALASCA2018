package fr.upmc.gaspardleo.admissioncontroller;

import java.util.ArrayList;

import fr.upmc.components.AbstractComponent;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.cvm.connectors.CVMConnector;
import fr.upmc.gaspardleo.cvm.ports.CVMOutboundPort;

public class AdmissionController 
		extends AbstractComponent
		implements AdmissionControllerI{
	
	private	CVMOutboundPort 				cvmop;
	private ArrayList<RequestDispatcher>	RDs;
	
	//TODO delete RD avec unregisterVM
	
	public AdmissionController(String CVM_InboundPorURI) throws Exception{
		super(1, 1);
		this.RDs = new ArrayList<RequestDispatcher>();
		
		//TODO via connecteur ?
		
		// Connect Admission Controller with CVM Component
		connectionWithCVM(CVM_InboundPorURI);
	}
	
	@Override
	public String addRequestSource(
			String RD_URI,
			String RG_RequestNotificationInboundPortURI) throws Exception {
		
		// Request Dispatcher creation	
		String RD_RequestSubmissionInboundPortURI = createRequestDispatcher(
				RD_URI, RG_RequestNotificationInboundPortURI);

		return RD_RequestSubmissionInboundPortURI;
	}
	
	private void connectionWithCVM(String CVM_InboundPorURI) throws Exception{
		
		// Connections Admission Controller with CVM Component
		this.cvmop = new CVMOutboundPort(this);
		this.addPort(this.cvmop);
		this.cvmop.publishPort();
		this.cvmop.doConnection(
				CVM_InboundPorURI,
				CVMConnector.class.getCanonicalName());
	}
	
	private String createRequestDispatcher(
			String RD_URI, String RG_RequestNotificationInboundPortURI) throws Exception{
				
		// Request Dispatcher creation
		RequestDispatcher rd = new RequestDispatcher(RD_URI, RG_RequestNotificationInboundPortURI);
		this.RDs.add(rd);
		
		// Request Dispatcher debug
		rd.toggleLogging();
		rd.toggleTracing();
		
		// Deploy Request Dispatcher
		this.cvmop.deployComponent(rd);
		
		String numRD = RD_URI.split("-")[1];
		
		// Vm applications creation
		ApplicationVM vm0 = createApplicationVM("vm-" + numRD + "-0");
		ApplicationVM vm1 = createApplicationVM("vm-" + numRD + "-1");
		ApplicationVM vm2 = createApplicationVM("vm-" + numRD + "-2");
		
		// Register application VM in Request Dispatcher
		rd.registerVM(
				vm0.getAVMPortsURI().get(ApplicationVMPortTypes.INTROSPECTION),
				vm0.getAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));
		rd.registerVM(
				vm1.getAVMPortsURI().get(ApplicationVMPortTypes.INTROSPECTION),
				vm1.getAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));
		rd.registerVM(
				vm2.getAVMPortsURI().get(ApplicationVMPortTypes.INTROSPECTION),
				vm2.getAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));
		
		return rd.getRDPortsURI().get(RDPortTypes.REQUEST_SUBMISSION_IN);
	}
	
	private ApplicationVM createApplicationVM(String VM_URI) throws Exception{
				
		// Vm applications creation
		ApplicationVM vm = new ApplicationVM(VM_URI);
		
		// VM debug
		vm.toggleTracing();
		vm.toggleLogging();
		
		// Deploy VM
		this.cvmop.deployComponent(vm);
				
		// Create a mock up port to manage the AVM component (allocate cores).
		ApplicationVMManagementOutboundPort avmPort = new ApplicationVMManagementOutboundPort(
				new AbstractComponent(0, 0) {});
		avmPort.publishPort();
		
		avmPort.doConnection(
				vm.getAVMPortsURI().get(ApplicationVMPortTypes.MANAGEMENT),
				ApplicationVMManagementConnector.class.getCanonicalName());
		
		// Cores allocation
		this.cvmop.addAVMPort(avmPort);
				
		return vm;
	}
}