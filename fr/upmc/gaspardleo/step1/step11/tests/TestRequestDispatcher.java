package fr.upmc.gaspardleo.step1.step11.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.hardware.tests.ComputerMonitor;
import fr.upmc.gaspardleo.step0.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.step1.step11.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.step1.step11.requestdispatcher.RequestDispatcher.RDPortTypes;

public class TestRequestDispatcher extends AbstractCVM {

	// Computer ports
	private static final String ComputerServicesInboundPortURI = "csip";
	private static final String ComputerServicesOutboundPortURI = "csop";
	private static final String ComputerStaticStateDataInboundPortURI = "cssdip";
	private static final String ComputerStaticStateDataOutboundPortURI = "cssdop";
	private static final String ComputerDynamicStateDataInboundPortURI = "cdsdip";
	private static final String ComputerDynamicStateDataOutboundPortURI = "cdsdop";

	// Rg ports
	private static final String RG_RequestGeneratorManagementInboundPortURI = "rg-rgmip";
	private static final String RG_RequestSubmissionOutboundPortURI = "rg-rsop";
	private static final String RG_RequestNotificationInboundPortURI = "rg-rnip";
	
	//RGM port
	private static final String RGM_RequestGeneratorManagementOutboundPortURI = "rgm-rgmop";

	// Components
	private ComputerMonitor cm;
	private ApplicationVM vm0, vm1, vm2;
	private RequestGenerator rg;
	private RequestDispatcher rd;

	// Ports
	private ComputerServicesOutboundPort csPort ;
	private ApplicationVMManagementOutboundPort avmPort0, avmPort1, avmPort2;
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


		// Vm applications creation
		this.vm0 = new ApplicationVM("vm0",
				AbstractPort.generatePortURI(),
				AbstractPort.generatePortURI(),
				AbstractPort.generatePortURI());
		this.addDeployedComponent(this.vm0) ;

		
		// Create a mock up port to manage the AVM component (allocate cores).
		this.avmPort0 = new ApplicationVMManagementOutboundPort(new AbstractComponent(0, 0) {});
		this.avmPort0.publishPort() ;
		this.avmPort0.
		doConnection(
				vm0.getAVMPortsURI().get(ApplicationVMPortTypes.MANAGEMENT),
				ApplicationVMManagementConnector.class.getCanonicalName()) ;

		// VM debug
		this.vm0.toggleTracing();
		this.vm0.toggleLogging();
		
		//-------
		
		this.vm1 = new ApplicationVM("vm1",
				AbstractPort.generatePortURI(),
				AbstractPort.generatePortURI(),
				AbstractPort.generatePortURI());
		this.addDeployedComponent(this.vm1) ;

		
		// Create a mock up port to manage the AVM component (allocate cores).
		this.avmPort1 = new ApplicationVMManagementOutboundPort(
				new AbstractComponent(1, 1) {});
		this.avmPort1.publishPort() ;
		this.avmPort1.
		doConnection(
				vm1.getAVMPortsURI().get(ApplicationVMPortTypes.MANAGEMENT),
				ApplicationVMManagementConnector.class.getCanonicalName()) ;

		// VM debug
		this.vm1.toggleTracing();
		this.vm1.toggleLogging();

		
		//------
		
		this.vm2 = new ApplicationVM("vm2",
				AbstractPort.generatePortURI(),
				AbstractPort.generatePortURI(),
				AbstractPort.generatePortURI());
		this.addDeployedComponent(this.vm2) ;

		
		// Create a mock up port to manage the AVM component (allocate cores).
		this.avmPort2 = new ApplicationVMManagementOutboundPort(
				new AbstractComponent(2, 2) {});
		this.avmPort2.publishPort() ;
		this.avmPort2.
		doConnection(
				vm2.getAVMPortsURI().get(ApplicationVMPortTypes.MANAGEMENT),
				ApplicationVMManagementConnector.class.getCanonicalName()) ;

		// VM debug
		this.vm2.toggleTracing();
		this.vm2.toggleLogging();

		
		
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
		this.rd = new RequestDispatcher("rd0");
		this.addDeployedComponent(rd);
		
		// Rd debug
		this.rd.toggleLogging();
		this.rd.toggleTracing();
		
		
		
		
		// Port connections
		this.rg.doPortConnection(
				RG_RequestSubmissionOutboundPortURI,
				//RD_RequestSubmissionInboundPortURI,
				rd.getRDPortsURI().get(RDPortTypes.REQUEST_SUBMISSION_IN),
				RequestSubmissionConnector.class.getCanonicalName());
		
		this.rd.doPortConnection(
				//RD_RequestNotificationOutboundPortURI,
				rd.getRDPortsURI().get(RDPortTypes.REQUEST_NOTIFICATION_OUT),
				RG_RequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName());
		
		this.rd.registerVM("vm0", vm0.getAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));
		this.rd.registerVM("vm1", vm1.getAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));
		this.rd.registerVM("vm2", vm2.getAVMPortsURI().get(ApplicationVMPortTypes.REQUEST_SUBMISSION));

		
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

		AllocatedCore[] ac = this.csPort.allocateCores(4);
		this.avmPort0.allocateCores(new AllocatedCore[] {ac[0]});
		this.avmPort1.allocateCores(new AllocatedCore[] {ac[1]});
		this.avmPort2.allocateCores(new AllocatedCore[] {ac[2], ac[3]});
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
			}).start();
			
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
