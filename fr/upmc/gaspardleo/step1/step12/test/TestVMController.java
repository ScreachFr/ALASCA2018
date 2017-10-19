package fr.upmc.gaspardleo.step1.step12.test;

import fr.upmc.components.examples.basic_cs.CVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.step1.step12.cvm.CVMComponent;

public class TestVMController {
	
	private static String CVM_IPURI 									= AbstractPort.generatePortURI();
	
	private static String RG1_RequestGeneratorManagementInboundPortURI	= AbstractPort.generatePortURI();
	private static String RG1_RequestSubmissionOutboundPortURI			= AbstractPort.generatePortURI();
	private static String RG1_RequestNotificationInboundPortURI			= AbstractPort.generatePortURI();
	
	private static String RG2_RequestGeneratorManagementInboundPortURI	= AbstractPort.generatePortURI();
	private static String RG2_RequestSubmissionOutboundPortURI			= AbstractPort.generatePortURI();
	private static String RG2_RequestNotificationInboundPortURI			= AbstractPort.generatePortURI();
	
	private CVM 				cvm;
	private CVMComponent 		cvmc;
	private RequestGenerator 	rg1, rg2;
	private AdmissionController ac1, ac2;
	
	public TestVMController(){
		initTest();
	}
	
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
					RG1_RequestNotificationInboundPortURI) ;

			// Rg debug
			this.rg1.toggleTracing() ;
			this.rg1.toggleLogging() ;
			
			// Admission Controller creation
			this.ac1 = new AdmissionController();
			
			// Components deployment
			this.cvm.deploy();
			this.cvmc.deployComponent(rg1);
			this.cvmc.deployComponent(ac1);
			
			// Dynamic ressources creation
			this.ac1.addRequestSource(
					RG1_RequestSubmissionOutboundPortURI, 
					RG1_RequestNotificationInboundPortURI, 
					RG1_RequestGeneratorManagementInboundPortURI, 
					CVM_IPURI);
			
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
					RG2_RequestNotificationInboundPortURI) ;
			
			// Rg debug
			this.rg2.toggleTracing() ;
			this.rg2.toggleLogging() ;
			
			// Admission Controller creation
			this.ac2 = new AdmissionController();
			
			// Components deployment
			this.cvm.deploy();
			this.cvmc.deployComponent(rg2);
			this.cvmc.deployComponent(ac2);
			
			// Ressources dynamic creation
			this.ac2.addRequestSource(
					RG2_RequestSubmissionOutboundPortURI, 
					RG2_RequestNotificationInboundPortURI, 
					RG2_RequestGeneratorManagementInboundPortURI, 
					CVM_IPURI);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		
	}
}