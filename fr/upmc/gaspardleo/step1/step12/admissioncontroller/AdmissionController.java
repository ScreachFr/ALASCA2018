package fr.upmc.gaspardleo.step1.step12.admissioncontroller;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.gaspardleo.step1.step11.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.step1.step12.cvm.connectors.CVMConnector;
import fr.upmc.gaspardleo.step1.step12.cvm.ports.CVMOutboundPort;

public class AdmissionController 
		extends AbstractComponent
		implements AdmissionControllerI{
	
	private CVMOutboundPort 	cvmop;
	private RequestDispatcher 	rd;
	
	public AdmissionController(){
		super(1, 1);
	}
	
	@Override
	public String addRequestSource(
			String RG_RequestSubmissionOutboundPortURI,
			String RG_RequestNotificationInboundPortURI,
			String RG_RequestGeneratorManagementInboundPortURI,
			String CVM_InboundPorURI) throws Exception {
								
		// Connections Admission Controller with CVM Component
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
		
		String AC_CMVOutboundPorURI = AbstractPort.generatePortURI();
		
		// Connections Admission Controller with CVM Component
		this.cvmop = new CVMOutboundPort(
				AC_CMVOutboundPorURI,
				this);
		
		this.addPort(this.cvmop);
		this.cvmop.publishPort();
		this.cvmop.doConnection(
				CVM_InboundPorURI,
				CVMConnector.class.getCanonicalName());
	}
	
	private String createRequestDispatcher(String RG_RequestNotificationInboundPortURI) throws Exception{
		
		String RD_RequestSubmissionInboundPortURI 				= AbstractPort.generatePortURI();
		String RD_RequestSubmissionOutboundPortURI 				= AbstractPort.generatePortURI();
		String RD_RequestNotificationInboundPortURI 			= AbstractPort.generatePortURI();
		String RD_RequestNotificationOutboundPortURI 			= AbstractPort.generatePortURI();
		
		// Request Dispatcher creation
		this.rd = new RequestDispatcher("rd",	//TODO change name "rd"
				RD_RequestSubmissionInboundPortURI, 
				RD_RequestSubmissionOutboundPortURI,
				RD_RequestNotificationInboundPortURI, 
				RD_RequestNotificationOutboundPortURI);
		
		// Request Dispatcher debug
		this.rd.toggleLogging();
		this.rd.toggleTracing();
		
		// Deploy Request Dispatcher
		this.cvmop.deployComponent(this.rd);
		
		// Connections Request Dispatcher with Request Generator
		RequestNotificationOutboundPort rnop = new RequestNotificationOutboundPort(
				RD_RequestNotificationOutboundPortURI, 
				this.rd);
		this.addPort(rnop);
		rnop.publishPort();
		rnop.doConnection(
				RG_RequestNotificationInboundPortURI, 
				RequestNotificationConnector.class.getCanonicalName());
		
		return RD_RequestSubmissionInboundPortURI;
	}
	
	private ApplicationVM createApplicationVM() throws Exception{
		
		ApplicationVM vm;
		ApplicationVMManagementOutboundPort avmPort;
		
		String ApplicationVMManagementInboundPortURI 		= AbstractPort.generatePortURI();
		String RequestSubmissionInboundPortURI 				= AbstractPort.generatePortURI();
		String RequestNotificationOutboundPortURI 			= AbstractPort.generatePortURI();
		String ApplicationVMManagementOutboundPortURI 		= AbstractPort.generatePortURI();
		
		// Vm applications creation
		vm = new ApplicationVM("vm",				// application vm component URI	//TODO change name "vm"
				ApplicationVMManagementInboundPortURI,
				RequestSubmissionInboundPortURI,
				RequestNotificationOutboundPortURI);
		
		// Create a mock up port to manage the AVM component (allocate cores).
		avmPort = new ApplicationVMManagementOutboundPort(
				ApplicationVMManagementOutboundPortURI,
				new AbstractComponent(0, 0) {});
		avmPort.publishPort();
		avmPort.
		doConnection(
				ApplicationVMManagementInboundPortURI,
				ApplicationVMManagementConnector.class.getCanonicalName());

		// VM debug
		vm.toggleTracing();
		vm.toggleLogging();
		
		// Cores allocation
		this.cvmop.addAVMPort(avmPort);
		
		// Deploy VM
		this.cvmop.deployComponent(vm);
		
		// Register application VM in Request Dispatcher
		this.rd.registerVM("vm", 							//TODO change name "vm"
				RequestSubmissionInboundPortURI);
		
		return vm;
	}
}