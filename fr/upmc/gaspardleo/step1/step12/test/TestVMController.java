package fr.upmc.gaspardleo.step1.step12.test;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.step1.step12.cvm.CVM;
import fr.upmc.gaspardleo.step1.step12.cvm.CVMComponent;

public class TestVMController {
	
	private static String CVM_IPURI 											= AbstractPort.generatePortURI();
	
	private static final String RG1_RequestGeneratorManagementInboundPortURI	= AbstractPort.generatePortURI();
	private static final String RG1_RequestSubmissionOutboundPortURI			= AbstractPort.generatePortURI();
	private static final String RG1_RequestNotificationOutboundPortURI			= AbstractPort.generatePortURI();
	private static final String RG1_RequestNotificationInboundPortURI			= AbstractPort.generatePortURI();
	
	private static final String RG2_RequestGeneratorManagementInboundPortURI	= AbstractPort.generatePortURI();
	private static final String RG2_RequestSubmissionOutboundPortURI			= AbstractPort.generatePortURI();
	private static final String RG2_RequestNotificationOutboundPortURI			= AbstractPort.generatePortURI();
	private static final String RG2_RequestNotificationInboundPortURI			= AbstractPort.generatePortURI();
	
	private static final String RGM1_RequestGeneratorManagementOutboundPortURI 	= AbstractPort.generatePortURI();
	private static final String RGM2_RequestGeneratorManagementOutboundPortURI 	= AbstractPort.generatePortURI();
	
	private String RD1_RequestSubmissionInboundPortURI;
	private String RD2_RequestSubmissionInboundPortURI;
	
	private static CVM 									cvm;
	private CVMComponent 								cvmc;
	private RequestGenerator 							rg1, rg2;
	private AdmissionController 						ac1, ac2;
	private RequestGeneratorManagementOutboundPort 		rgmop1, rgmop2;
	
	public TestVMController(){
		initTest();
	}
	
	@SuppressWarnings("static-access")
	private void initTest(){
		try {	
			
			// CVM creation
			this.cvm 	= new CVM();
			
			// CVM Component creation
			this.cvmc 	= new CVMComponent(cvm, CVM_IPURI);
			
			
			///////////////////////////
			///		Application 1	///
			///////////////////////////
			
			// Request Generator creation
			this.rg1 = new RequestGenerator(
					"rg1",										// generator component URI
					500.0,										// mean time between two requests
					6000000000L,								// mean number of instructions in requests
					RG1_RequestGeneratorManagementInboundPortURI,
					RG1_RequestSubmissionOutboundPortURI,
					RG1_RequestNotificationInboundPortURI);

			// Rg debug
			this.rg1.toggleTracing();
			this.rg1.toggleLogging();
			
			// Admission Controller creation
			this.ac1 = new AdmissionController();
			
			// Components deployment
			//TODO this.cvm.deploy();
			this.cvmc.deployComponent(rg1);
			this.cvmc.deployComponent(ac1);
			
			// Dynamic ressources creation
			RD1_RequestSubmissionInboundPortURI = this.ac1.addRequestSource(
					RG1_RequestSubmissionOutboundPortURI,
					RG1_RequestNotificationInboundPortURI,
					RG1_RequestGeneratorManagementInboundPortURI, 
					CVM_IPURI);
			
			RequestSubmissionOutboundPort rsop1 = new RequestSubmissionOutboundPort(
					RG1_RequestSubmissionOutboundPortURI, 
					rg1);
			this.cvmc.addPort(rsop1);
			rsop1.publishPort();
			rsop1.doConnection(
					RD1_RequestSubmissionInboundPortURI, 
					RequestSubmissionConnector.class.getCanonicalName());
			
			// Rg management creation
			this.rgmop1 = new RequestGeneratorManagementOutboundPort(
					RGM1_RequestGeneratorManagementOutboundPortURI,
					new AbstractComponent(0, 0) {});
			this.rgmop1.publishPort();
			this.rgmop1.doConnection(
					RG1_RequestGeneratorManagementInboundPortURI,
					RequestGeneratorManagementConnector.class.getCanonicalName());
			
			///////////////////////////
			///		Application 2	///
			///////////////////////////
			
			// Request Generator creation
			this.rg2 = new RequestGenerator(
					"rg2",										// generator component URI
					500.0,										// mean time between two requests
					6000000000L,								// mean number of instructions in requests
					RG2_RequestGeneratorManagementInboundPortURI,
					RG2_RequestSubmissionOutboundPortURI,
					RG2_RequestNotificationInboundPortURI);
			
			// Rg debug
			this.rg2.toggleTracing();
			this.rg2.toggleLogging();
			
			// Admission Controller creation
			this.ac2 = new AdmissionController();
			
			// Components deployment
			this.cvm.deploy();
			this.cvmc.deployComponent(rg2);
			this.cvmc.deployComponent(ac2);
			
			// Ressources dynamic creation
			RD2_RequestSubmissionInboundPortURI = this.ac2.addRequestSource( 
					RG2_RequestSubmissionOutboundPortURI,
					RG2_RequestNotificationInboundPortURI,
					RG2_RequestGeneratorManagementInboundPortURI, 
					CVM_IPURI);
			
			// Rg management creation
			this.rgmop2 = new RequestGeneratorManagementOutboundPort(
					RGM2_RequestGeneratorManagementOutboundPortURI,
					new AbstractComponent(0, 0) {});
			this.rgmop2.publishPort();
			this.rgmop2.doConnection(
					RG2_RequestGeneratorManagementInboundPortURI,
					RequestGeneratorManagementConnector.class.getCanonicalName());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testScenario() throws Exception {
		
		// start the request generation in the request generator.
		this.rgmop1.startGeneration();
		this.rgmop2.startGeneration();
		
		// wait 20 seconds
		Thread.sleep(20000L);
		// then stop the generation.
		
		this.rgmop1.stopGeneration();
		this.rgmop2.stopGeneration();
	}
	
	public static void main(String[] args){
		
		// AbstractCVM.toggleDebugMode() ;
		try {
			final TestVMController tvmc = new TestVMController() ;
			// Deploy the components
			cvm.deploy() ;
			
			System.out.println("starting...") ;
			// Start them.
			cvm.start() ;
			
			// Execute the chosen request generation test scenario in a
			// separate thread.
			new Thread(() -> {
				try {
					tvmc.testScenario();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).start();;
			
			// Sleep to let the test scenario execute to completion.
			Thread.sleep(90000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
			cvm.shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}