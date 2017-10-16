package fr.upmc.gaspardleo.step1.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.hardware.tests.ComputerMonitor;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.step1.requestdispatcher.RequestDispatcher;

public class TestRequestDispatcher extends AbstractCVM {

	// Computer ports
	private static final String ComputerServicesInboundPortURI = "csip";
	private static final String ComputerServicesOutboundPortURI = "csop";
	private static final String ComputerStaticStateDataInboundPortURI = "cssdip";
	private static final String ComputerStaticStateDataOutboundPortURI = "cssdop";
	private static final String ComputerDynamicStateDataInboundPortURI = "cdsdip";
	private static final String ComputerDynamicStateDataOutboundPortURI = "cdsdop";

	// Vm ports
	private static final String VM_ApplicationVMManagementInboundPortURI = "vm-avmmip";
	private static final String VM_RequestSubmissionInboundPortURI = "vm-rsip";
	private static final String VM_RequestNotificationOutboundPortURI = "vm-rnop";
	private static final String VM_ApplicationVMManagementOutboundPortURI = "vm-avmmop";

	// Rg ports
	private static final String RG_RequestGeneratorManagementInboundPortURI = "rg-rgmip";
	private static final String RG_RequestSubmissionOutboundPortURI = "rg-rsop";
	private static final String RG_RequestNotificationInboundPortURI = "rg-rnip";

	
	//RGM port
	private static final String RGM_RequestGeneratorManagementOutboundPortURI = "rgm-rgmop";

	
	//RD ports
	private static final String RD_RequestSubmissionInboundPortURI = "rd-rsip";
	private static final String RD_RequestSubmissionOutboundPortURI = "rd-rsop";
	private static final String RD_RequestNotificationInboundPortURI = "rd-rnip";
	private static final String RD_RequestNotificationOutboundPortURI = "rd-rnop";
	

	// Components
	private ComputerMonitor cm;
	private ApplicationVM vm;
	private RequestGenerator rg;
	private RequestDispatcher rd;

	// Ports
	private ComputerServicesOutboundPort csPort ;
	private ApplicationVMManagementOutboundPort avmPort;
	private RequestGeneratorManagementOutboundPort rgmop;

	public TestRequestDispatcher() throws Exception {
		super();
	}

	@Override
	public void deploy() throws Exception {
		AbstractComponent.configureLogging("", "", 0, '|');
		Processor.DEBUG = true;

		// Computer creation
		String computerURI = "computer0";
		int numberOfProcessors = 2;
		int numberOfCores = 2;
		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500);	// Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000);	// and at 3 GHz
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000);	// 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000);	// 3 GHz executes 3 Mips
		Computer c = new Computer(
				computerURI,
				admissibleFrequencies,
				processingPower,  
				1500,		// Test scenario 1, frequency = 1,5 GHz
				// 3000,	// Test scenario 2, frequency = 3 GHz
				1500,		// max frequency gap within a processor
				numberOfProcessors,
				numberOfCores,
				ComputerServicesInboundPortURI,
				ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI);
		this.addDeployedComponent(c);

		this.csPort = new ComputerServicesOutboundPort(
				ComputerServicesOutboundPortURI,
				new AbstractComponent(0, 0) {}) ;
		this.csPort.publishPort();
		this.csPort.doConnection(
				ComputerServicesInboundPortURI,
				ComputerServicesConnector.class.getCanonicalName());

		this.cm = new ComputerMonitor(computerURI,
				true,
				ComputerStaticStateDataOutboundPortURI,
				ComputerDynamicStateDataOutboundPortURI) ;
		this.addDeployedComponent(this.cm) ;

		this.cm.doPortConnection(
				ComputerStaticStateDataOutboundPortURI,
				ComputerStaticStateDataInboundPortURI,
				DataConnector.class.getCanonicalName()) ;

		this.cm.doPortConnection(
				ComputerDynamicStateDataOutboundPortURI,
				ComputerDynamicStateDataInboundPortURI,
				ControlledDataConnector.class.getCanonicalName()) ;


		// Vm application creation
		this.vm = new ApplicationVM("vm0",	// application vm component URI
				VM_ApplicationVMManagementInboundPortURI,
				VM_RequestSubmissionInboundPortURI,
				VM_RequestNotificationOutboundPortURI) ;
		this.addDeployedComponent(this.vm) ;


		// Create a mock up port to manage the AVM component (allocate cores).
		this.avmPort = new ApplicationVMManagementOutboundPort(
				VM_ApplicationVMManagementOutboundPortURI,
				new AbstractComponent(0, 0) {}) ;
		this.avmPort.publishPort() ;
		this.avmPort.
		doConnection(
				VM_ApplicationVMManagementInboundPortURI,
				ApplicationVMManagementConnector.class.getCanonicalName()) ;


		// VM debug
		this.vm.toggleTracing() ;
		this.vm.toggleLogging() ;

		// Request Generator creation
		this.rg = new RequestGenerator(
				"rg",			// generator component URI
				500.0,			// mean time between two requests
				6000000000L,	// mean number of instructions in requests
				RG_RequestGeneratorManagementInboundPortURI,
				RG_RequestSubmissionOutboundPortURI,
				RG_RequestNotificationInboundPortURI) ;
		this.addDeployedComponent(rg) ;

		// Rg debug
		this.rg.toggleTracing() ;
		this.rg.toggleLogging() ;

		
		// Request Dispatcher creation
		this.rd = new RequestDispatcher("rd0",
				RD_RequestSubmissionInboundPortURI, RD_RequestSubmissionOutboundPortURI,
				RD_RequestNotificationInboundPortURI, RD_RequestNotificationOutboundPortURI);
		this.addDeployedComponent(rd);
		
		// Rd debug
		this.rd.toggleLogging();
		this.rd.toggleTracing();
		
		
		
		// Port connections
		this.rg.doPortConnection(
				RG_RequestSubmissionOutboundPortURI,
				RD_RequestSubmissionInboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		this.rd.doPortConnection(
				RD_RequestSubmissionOutboundPortURI,
				VM_RequestSubmissionInboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		this.vm.doPortConnection(
				VM_RequestNotificationOutboundPortURI,
				RD_RequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName()) ;
		
		this.rd.doPortConnection(RD_RequestNotificationOutboundPortURI,
				RG_RequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName());
		
//		this.rg.doPortConnection(
//				RG_RequestSubmissionOutboundPortURI,
//				VM_RequestSubmissionInboundPortURI,
//				RequestSubmissionConnector.class.getCanonicalName()) ;

//		this.vm.doPortConnection(
//				VM_RequestNotificationOutboundPortURI,
//				RG_RequestNotificationInboundPortURI,
//				RequestNotificationConnector.class.getCanonicalName()) ;

		// Rg management creation
		this.rgmop = new RequestGeneratorManagementOutboundPort(
				RGM_RequestGeneratorManagementOutboundPortURI,
				new AbstractComponent(0, 0) {}) ;
		this.rgmop.publishPort() ;
		this.rgmop.doConnection(
				RG_RequestGeneratorManagementInboundPortURI,
				RequestGeneratorManagementConnector.class.getCanonicalName()) ;


		super.deploy();
	}

	@Override
	public void start() throws Exception {
		super.start();

		AllocatedCore[] ac = this.csPort.allocateCores(4) ;
		this.avmPort.allocateCores(ac) ;
	}

	
	public void testScenario() throws Exception {
		// start the request generation in the request generator.
		this.rgmop.startGeneration() ;
		// wait 20 seconds
		Thread.sleep(20000L) ;
		// then stop the generation.
		this.rgmop.stopGeneration() ;
	}


	public static void main(String[] args) {
//		AbstractCVM.toggleDebugMode() ;
		try {
			final TestRequestDispatcher trd = new TestRequestDispatcher() ;
			// Deploy the components
			trd.deploy() ;
			
			System.out.println("starting...") ;
			// Start them.
			trd.start() ;
			
			// Execute the chosen request generation test scenario in a
			// separate thread.
			new Thread(() -> {
				try {
					trd.testScenario();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).start();;
			
			// Sleep to let the test scenario execute to completion.
			Thread.sleep(90000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
			trd.shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

}
