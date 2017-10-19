package fr.upmc.gaspardleo.step1.step12.admissioncontroller;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.step1.step11.requestdispatcher.RequestDispatcher;

public class AdmissionController 
		extends AbstractComponent{

	private AbstractCVM cvm;
	private int VMcreationCounter = 0;
	
	public AdmissionController(AbstractCVM cvm){
		super(1, 1);
		
		//TODO
		this.cvm = cvm;
	}
	
	private void createRequestDispatcher() throws Exception {
		
		RequestDispatcher rd;
		
		String RD_RequestSubmissionInboundPortURI 		= AbstractPort.generatePortURI();
		String RD_RequestSubmissionOutboundPortURI 		= AbstractPort.generatePortURI();
		String RD_RequestNotificationInboundPortURI 	= AbstractPort.generatePortURI();
		String RD_RequestNotificationOutboundPortURI	= AbstractPort.generatePortURI();	
		
		// request dispatcher component URI
		rd = new RequestDispatcher("rd" + VMcreationCounter,
				RD_RequestSubmissionInboundPortURI, 
				RD_RequestSubmissionOutboundPortURI,
				RD_RequestNotificationInboundPortURI, 
				RD_RequestNotificationOutboundPortURI);
		this.cvm.addDeployedComponent(rd);
		
		// Rd debug
		rd.toggleLogging();
		rd.toggleTracing();
		
	}
	
	private void createApplicationVM() throws Exception {
		
		ApplicationVM vm;
		ApplicationVMManagementOutboundPort avmPort;
		
		String VM_ApplicationVMManagementInboundPortURI 	= AbstractPort.generatePortURI();
		String VM_RequestSubmissionInboundPortURI 			= AbstractPort.generatePortURI();
		String VM_RequestNotificationOutboundPortURI 		= AbstractPort.generatePortURI();
		String VM_ApplicationVMManagementOutboundPortURI 	= AbstractPort.generatePortURI();
		
		// application vm component URI
		vm = new ApplicationVM("vm" + VMcreationCounter ,
				VM_ApplicationVMManagementInboundPortURI,
				VM_RequestSubmissionInboundPortURI,
				VM_RequestNotificationOutboundPortURI) ;
		this.cvm.addDeployedComponent(vm) ;

		
		// Create a mock up port to manage the AVM component (allocate cores).
		avmPort = new ApplicationVMManagementOutboundPort(
				VM_ApplicationVMManagementOutboundPortURI,
				new AbstractComponent(0, 0) {});
		avmPort.publishPort() ;
		avmPort.
		doConnection(
				VM_ApplicationVMManagementInboundPortURI,
				ApplicationVMManagementConnector.class.getCanonicalName()) ;

		// VM debug
		vm.toggleTracing();
		vm.toggleLogging();
	}
	
	
	
	private void createComponents() throws Exception {
		
		//-----
		ApplicationVM vm;
		ApplicationVMManagementOutboundPort avmPort;
		
		String VM_ApplicationVMManagementInboundPortURI 		= "vm" + VMcreationCounter + "-avmmip";
		String VM_RequestSubmissionInboundPortURI 				= "vm" + VMcreationCounter + "-rsip";
		String VM_RequestNotificationOutboundPortURI 			= "vm" + VMcreationCounter + "-rnop";
		String VM_ApplicationVMManagementOutboundPortURI 		= "vm" + VMcreationCounter + "-avmmop";
		
		//-----
		RequestGenerator rg;
		
		String RG_RequestGeneratorManagementInboundPortURI 		= "rg" + VMcreationCounter + "-rgmip";
		String RG_RequestSubmissionOutboundPortURI 				= "rg" + VMcreationCounter + "-rsop";
		String RG_RequestNotificationInboundPortURI 			= "rg" + VMcreationCounter + "-rnip";
		
		//-----
		RequestDispatcher rd;
		
		String RD_RequestSubmissionInboundPortURI 				= "rd" + VMcreationCounter + "-rsip";
		String RD_RequestSubmissionOutboundPortURI 				= "rd" + VMcreationCounter + "-rsop";
		String RD_RequestNotificationInboundPortURI 			= "rd" + VMcreationCounter + "-rnip";
		String RD_RequestNotificationOutboundPortURI 			= "rd" + VMcreationCounter + "-rnop";	
		
		//-----
		RequestGeneratorManagementOutboundPort rgmop;
		
		String RGM_RequestGeneratorManagementOutboundPortURI = "rgm" + VMcreationCounter + "-rgmop";
		
		
		//////////////////////////////////////////////////
		// Vm applications creation
		
		vm = new ApplicationVM("vm" + VMcreationCounter ,	// application vm component URI
				VM_ApplicationVMManagementInboundPortURI,
				VM_RequestSubmissionInboundPortURI,
				VM_RequestNotificationOutboundPortURI) ;
		this.cvm.addDeployedComponent(vm) ;

		
		// Create a mock up port to manage the AVM component (allocate cores).
		avmPort = new ApplicationVMManagementOutboundPort(
				VM_ApplicationVMManagementOutboundPortURI,
				new AbstractComponent(0, 0) {});
		avmPort.publishPort() ;
		avmPort.
		doConnection(
				VM_ApplicationVMManagementInboundPortURI,
				ApplicationVMManagementConnector.class.getCanonicalName()) ;

		// VM debug
		vm.toggleTracing();
		vm.toggleLogging();
		
		//////////////////////////////////////////////////
		// Request Generator creation
		
		rg = new RequestGenerator(
				"rg" + VMcreationCounter,		// generator component URI
				500.0,							// mean time between two requests
				6000000000L,					// mean number of instructions in requests
				RG_RequestGeneratorManagementInboundPortURI,
				RG_RequestSubmissionOutboundPortURI,
				RG_RequestNotificationInboundPortURI) ;
		this.cvm.addDeployedComponent(rg) ;

		// Rg debug
		rg.toggleTracing() ;
		rg.toggleLogging() ;
		
		//////////////////////////////////////////////////
		// Request Dispatcher creation
		
		rd = new RequestDispatcher("rd" + VMcreationCounter,
				RD_RequestSubmissionInboundPortURI, 
				RD_RequestSubmissionOutboundPortURI,
				RD_RequestNotificationInboundPortURI, 
				RD_RequestNotificationOutboundPortURI);
		this.cvm.addDeployedComponent(rd);
		
		// Rd debug
		rd.toggleLogging();
		rd.toggleTracing();
		
		VMcreationCounter++;
		
		//////////////////////////////////////////////////
		// Port connections	
		
		rg.doPortConnection(
				RG_RequestSubmissionOutboundPortURI,
				RD_RequestSubmissionInboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		rd.doPortConnection(RD_RequestNotificationOutboundPortURI,
				RG_RequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName());
		
		rd.registerVM("vm0", VM_RequestSubmissionInboundPortURI);
		
		//////////////////////////////////////////////////
		// Rg management creation
		
		rgmop = new RequestGeneratorManagementOutboundPort(
				RGM_RequestGeneratorManagementOutboundPortURI,
				new AbstractComponent(0, 0) {}) ;
		rgmop.publishPort() ;
		rgmop.doConnection(
				RG_RequestGeneratorManagementInboundPortURI,
				RequestGeneratorManagementConnector.class.getCanonicalName()) ;
		
		//////////////////////////////////////////////////
		// deployment
		
		this.cvm.deploy();
	}
}
