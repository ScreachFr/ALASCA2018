package fr.upmc.gaspardleo.admissioncontroller;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.cvm.connectors.CVMConnector;
import fr.upmc.gaspardleo.cvm.ports.CVMOutboundPort;

public class AdmissionController 
		extends AbstractComponent
		implements AdmissionControllerI{
	
	private	CVMOutboundPort 	cvmop;
	private	RequestDispatcher 	rd;
	
	//TODO pool de RD
	//TODO delete RD avec unregisterVM
	
	public AdmissionController(){
		super(1, 1);
	}
	
	@Override
	public String addRequestSource(
			String RG_RequestNotificationInboundPortURI,
			String CVM_InboundPorURI) throws Exception {
								
		// Connect Admission Controller with CVM Component
		connectionWithCVM(CVM_InboundPorURI);
		
		// Request Dispatcher creation	
		String RD_RequestSubmissionInboundPortURI = createRequestDispatcher(RG_RequestNotificationInboundPortURI);
		
		// Vm applications creation

		createApplicationVM();
		createApplicationVM();
		createApplicationVM();
				
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
	
	//TODO URI RD en paramètre
	private String createRequestDispatcher(String RG_RequestNotificationInboundPortURI) throws Exception{
				
		// Request Dispatcher creation
		this.rd = new RequestDispatcher("rd");
		
		// Request Dispatcher debug
		this.rd.toggleLogging();
		this.rd.toggleTracing();
		
		// Deploy Request Dispatcher
		this.cvmop.deployComponent(this.rd);
		
		// Connections Request Dispatcher with Request Generator		
		RequestNotificationOutboundPort rnop = new RequestNotificationOutboundPort(this.rd);
		
		this.addPort(rnop);
		rnop.publishPort();
		
		//TODO déplacer dans le rd
		rnop.doConnection(
				RG_RequestNotificationInboundPortURI, 
				RequestNotificationConnector.class.getCanonicalName());
		
		return rd.getRDPortsURI().get(RDPortTypes.REQUEST_SUBMISSION_IN);
	}
	
	//TODO URI vm en paramètre
	private ApplicationVM createApplicationVM() throws Exception{
				
		// Vm applications creation
		ApplicationVM vm = new ApplicationVM("vm",
				AbstractPort.generatePortURI(),
				AbstractPort.generatePortURI(),
				AbstractPort.generatePortURI());
		
		// VM debug
		vm.toggleTracing();
		vm.toggleLogging();
		
		// Deploy VM
		this.cvmop.deployComponent(vm);
				
		// Register application VM in Request Dispatcher
		this.rd.registerVM(
				"vm",
				vm.getAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));
				
		// Create a mock up port to manage the AVM component (allocate cores).
		ApplicationVMManagementOutboundPort avmPort = new ApplicationVMManagementOutboundPort(
				new AbstractComponent(0, 0) {});
		avmPort.publishPort();
		
		avmPort.
		doConnection(
				vm.getAVMPortsURI().get(ApplicationVMPortTypes.MANAGEMENT),
				ApplicationVMManagementConnector.class.getCanonicalName());
		
		// Cores allocation
		this.cvmop.addAVMPort(avmPort);
				
		return vm;
	}
}