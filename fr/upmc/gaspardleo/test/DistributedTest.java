package fr.upmc.gaspardleo.test;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.connectors.AdmissionControllerConnector;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.cvm.CVM;
import fr.upmc.gaspardleo.cvm.CVMComponent;
import fr.upmc.gaspardleo.cvm.CVMComponent.CVMPortTypes;
import fr.upmc.gaspardleo.cvm.ports.CVMOutboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class DistributedTest 
	extends	AbstractDistributedCVM{
	
	private static final String Datacenter 			= "datacenter";
	private static final String DatacenterClient 	= "datacenterclient";
	
	public DistributedTest(String[] args) throws Exception {
		super(args);
	}
	
	private CVMComponent 							cvmcD;
	private CVM 									cvmD;
	private AdmissionController						ac;
	private RequestGenerator 						rg;
	private RequestGeneratorManagementOutboundPort 	rgmop;
	
	@Override
	public void instantiateAndPublish() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
			
			this.cvmD = new CVM();
			this.cvmcD = new CVMComponent(cvmD);
			this.addDeployedComponent(cvmcD);
			
			this.ac = new AdmissionController(
					this.cvmcD.getCVMPortsURI().get(CVMPortTypes.INTROSPECTION));
			this.addDeployedComponent(ac);
			
		} else {
			if (thisJVMURI.equals(DatacenterClient)){
				
				this.rg  = new RequestGenerator("rg");
				this.addDeployedComponent(rg);
				
				this.rgmop = new RequestGeneratorManagementOutboundPort(
					AbstractPort.generatePortURI(),
					new AbstractComponent(0, 0) {});
				this.rgmop.publishPort();
				this.rgmop.doConnection(
					rg.getRGPortsURI().get(RGPortTypes.MANAGEMENT_IN),
					RequestGeneratorManagementConnector.class.getCanonicalName());
			
			} else {
				throw new RuntimeException("unknown JVM " + thisJVMURI);
			}
		}
		super.instantiateAndPublish();
	}
	
	@Override
	public void interconnect() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
			
		} else {
			if (thisJVMURI.equals(DatacenterClient)){
				
				AdmissionControllerOutboundPort acop = new AdmissionControllerOutboundPort(
						this.ac.getACPortsURI().get(ACPortTypes.INTROSECTION),
						new AbstractComponent(0, 0) {});
				acop.publishPort();
				acop.doConnection(
						this.rg.getRGPortsURI().get(RGPortTypes.INTROSECTION),
						AdmissionControllerConnector.class.getCanonicalName());
				acop.addRequestSource(
						"rd",
						this.rg.getRGPortsURI().get(RGPortTypes.REQUEST_NOTIFICATION_IN));
				
				
			} else {
				throw new RuntimeException("unknown JVM " + thisJVMURI);
			}
		}
		super.interconnect();
	}
	
	@Override
	public void start() throws Exception {
		
		super.start();
		
		if(thisJVMURI.equals(Datacenter)){
						
		} else {
			if (thisJVMURI.equals(DatacenterClient)){
				
				CVMOutboundPort cvmop = new CVMOutboundPort(
						this.cvmcD.getCVMPortsURI().get(CVMPortTypes.INTROSPECTION),
						new AbstractComponent(0, 0) {});
					cvmop.publishPort();
					cvmop.start();
				
			} else {
				throw new RuntimeException("unknown JVM " + thisJVMURI);
			}
		}
		super.start();
	}
	
	public void testScenario() throws Exception {
		
		this.rgmop.startGeneration();
		Thread.sleep(20000L);
		this.rgmop.stopGeneration();
	}
	
	public static void main(String[] args){
		
		try {
			final DistributedTest dTest = new DistributedTest(args);
			dTest.deploy();
			
			System.out.println("starting...");
			dTest.start();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						dTest.testScenario();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}).start();;
			
			Thread.sleep(90000L);
			
			System.out.println("shutting down...");
			dTest.shutdown() ;
			
			System.out.println("ending...");
			System.exit(0) ;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}