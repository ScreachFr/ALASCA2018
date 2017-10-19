package fr.upmc.gaspardleo.step1.step12.admissioncontroller;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.step1.step11.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.step1.step12.cvm.connectors.CVMConnector;
import fr.upmc.gaspardleo.step1.step12.cvm.ports.CVMOutboundPort;

public class AdmissionController 
		extends AbstractComponent
		implements AdmissionControllerI{
	
	public AdmissionController(){
		super(1, 1);
	}
	
	@Override
	public void addRequestSource(
			String RG_RequestSubmissionOutboundPortURI, 
			String RG_RequestNotificationInboundPortURI,
			String RG_RequestGeneratorManagementInboundPortURI,
			String CVM_InboundPorURI) throws Exception {
		
		String AC_CVMInboundPorURI								= AbstractPort.generatePortURI();
		
		String VM0_ApplicationVMManagementInboundPortURI 		= AbstractPort.generatePortURI();
		String VM0_RequestSubmissionInboundPortURI 				= AbstractPort.generatePortURI();
		String VM0_RequestNotificationOutboundPortURI 			= AbstractPort.generatePortURI();
		String VM0_ApplicationVMManagementOutboundPortURI 		= AbstractPort.generatePortURI();
				
		String VM1_ApplicationVMManagementInboundPortURI 		= AbstractPort.generatePortURI();
		String VM1_RequestSubmissionInboundPortURI 				= AbstractPort.generatePortURI();
		String VM1_RequestNotificationOutboundPortURI 			= AbstractPort.generatePortURI();
		String VM1_ApplicationVMManagementOutboundPortURI 		= AbstractPort.generatePortURI();
		
		String VM2_ApplicationVMManagementInboundPortURI 		= AbstractPort.generatePortURI();
		String VM2_RequestSubmissionInboundPortURI 				= AbstractPort.generatePortURI();
		String VM2_RequestNotificationOutboundPortURI 			= AbstractPort.generatePortURI();
		String VM2_ApplicationVMManagementOutboundPortURI 		= AbstractPort.generatePortURI();
		
		String RD_RequestSubmissionInboundPortURI 				= AbstractPort.generatePortURI();
		String RD_RequestSubmissionOutboundPortURI 				= AbstractPort.generatePortURI();
		String RD_RequestNotificationInboundPortURI 			= AbstractPort.generatePortURI();
		String RD_RequestNotificationOutboundPortURI 			= AbstractPort.generatePortURI();
		
		String RGM_RequestGeneratorManagementOutboundPortURI 	= AbstractPort.generatePortURI();
		
		RequestDispatcher rd;
		ApplicationVM vm0, vm1, vm2;
		ApplicationVMManagementOutboundPort avmPort;
		RequestGeneratorManagementOutboundPort rgmop;
		
		// Vm applications creation
		vm0 = new ApplicationVM("vm0",	// application vm component URI
				VM0_ApplicationVMManagementInboundPortURI,
				VM0_RequestSubmissionInboundPortURI,
				VM0_RequestNotificationOutboundPortURI);
		
		// Create a mock up port to manage the AVM component (allocate cores).
		avmPort = new ApplicationVMManagementOutboundPort(
				VM0_ApplicationVMManagementOutboundPortURI,
				new AbstractComponent(0, 0) {});
		avmPort.publishPort();
		avmPort.
		doConnection(
				VM0_ApplicationVMManagementInboundPortURI,
				ApplicationVMManagementConnector.class.getCanonicalName());

		// VM debug
		vm0.toggleTracing();
		vm0.toggleLogging();
		
		//-------
		
		vm1 = new ApplicationVM("vm1",	// application vm component URI
				VM1_ApplicationVMManagementInboundPortURI,
				VM1_RequestSubmissionInboundPortURI,
				VM1_RequestNotificationOutboundPortURI);
		
		// Create a mock up port to manage the AVM component (allocate cores).
		avmPort = new ApplicationVMManagementOutboundPort(
				VM1_ApplicationVMManagementOutboundPortURI,
				new AbstractComponent(1, 1) {});
		avmPort.publishPort();
		avmPort.
		doConnection(
				VM1_ApplicationVMManagementInboundPortURI,
				ApplicationVMManagementConnector.class.getCanonicalName());

		// VM debug
		vm1.toggleTracing();
		vm1.toggleLogging();

		//------
		
		vm2 = new ApplicationVM("vm2",	// application vm component URI
				VM2_ApplicationVMManagementInboundPortURI,
				VM2_RequestSubmissionInboundPortURI,
				VM2_RequestNotificationOutboundPortURI);
		
		// Create a mock up port to manage the AVM component (allocate cores).
		avmPort = new ApplicationVMManagementOutboundPort(
				VM2_ApplicationVMManagementOutboundPortURI,
				new AbstractComponent(2, 2) {});
		avmPort.publishPort();
		avmPort.
		doConnection(
				VM2_ApplicationVMManagementInboundPortURI,
				ApplicationVMManagementConnector.class.getCanonicalName());

		// VM debug
		vm2.toggleTracing();
		vm2.toggleLogging();
		
		// Request Dispatcher creation
		rd = new RequestDispatcher("rd0",
				RD_RequestSubmissionInboundPortURI, 
				RD_RequestSubmissionOutboundPortURI,
				RD_RequestNotificationInboundPortURI, 
				RD_RequestNotificationOutboundPortURI);
		
		// Rd debug
		rd.toggleLogging();
		rd.toggleTracing();
		
		// Connections Admission Controller with CVM Component
		
		CVMOutboundPort cvmop = new CVMOutboundPort(
				AC_CVMInboundPorURI, 
				this);
		this.addPort(cvmop);
		cvmop.publishPort();
		cvmop.doConnection(
				CVM_InboundPorURI, 
				CVMConnector.class.getCanonicalName());
		
		// Deploy all components
		
		cvmop.deployComponent(vm0);
		cvmop.deployComponent(vm1);
		cvmop.deployComponent(vm2);
		cvmop.deployComponent(rd);

		// Connections Request Dispatcher with Request Generator
		
		RequestSubmissionInboundPort rsip = new RequestSubmissionInboundPort(
				RD_RequestSubmissionInboundPortURI,
				rd);
		this.addPort(rsip);
		rsip.publishPort();
		rsip.doConnection(
				RG_RequestSubmissionOutboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		RequestNotificationInboundPort rnip = new RequestNotificationInboundPort(
				RD_RequestNotificationOutboundPortURI,
				rd);
		this.addPort(rnip);
		rnip.publishPort();
		rnip.doConnection(
				RG_RequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName());
		
		// Register all applications VM in Request Dispatcher
		
		rd.registerVM("vm0", VM0_RequestSubmissionInboundPortURI);
		rd.registerVM("vm1", VM1_RequestSubmissionInboundPortURI);
		rd.registerVM("vm2", VM2_RequestSubmissionInboundPortURI);

		// Rg management creation
		rgmop = new RequestGeneratorManagementOutboundPort(
				RGM_RequestGeneratorManagementOutboundPortURI,
				new AbstractComponent(0, 0) {});
		rgmop.publishPort();
		rgmop.doConnection(
				RG_RequestGeneratorManagementInboundPortURI,
				RequestGeneratorManagementConnector.class.getCanonicalName());
	}
}