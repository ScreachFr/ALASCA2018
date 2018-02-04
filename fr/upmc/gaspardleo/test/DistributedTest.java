package fr.upmc.gaspardleo.test;

import java.util.HashMap;
import java.util.HashSet;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.computer.Computer;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public 	class 	DistributedTest 
		extends	AbstractDistributedCVM {

	private static final String Datacenter = "datacenter";
	private static final String DatacenterClient = "datacenterclient";
	private static final int NB_DATASOURCE = 1;
	private static final String AC_URI = "AC_URI";
	private HashMap<ACPortTypes, String> ac_uris;
	
	public DistributedTest(String[] args) throws Exception {
		super(args);
		ac_uris = AdmissionController.makeUris(AC_URI);
	}
		
	@Override
	public void start() throws Exception {
				
		if(thisJVMURI.equals(Datacenter)){
			
			HashMap<ComputerPoolPorts, String> computerPool_uris = ComputerPool.makeUris();
			ComputerPool cp = new ComputerPool(computerPool_uris);
			this.addDeployedComponent(cp);
			cp.start();
			
			HashSet<Integer> admissibleFrequencies = Computer.makeFrequencies();
			HashMap<Integer,Integer> processingPower = Computer.makeProcessingPower();
			
			Computer c0 = new Computer(Computer.makeUris(0), computerPool_uris, admissibleFrequencies, processingPower);
			Computer c1 = new Computer(Computer.makeUris(1), computerPool_uris, admissibleFrequencies, processingPower);
			Computer c2 = new Computer(Computer.makeUris(2), computerPool_uris, admissibleFrequencies, processingPower);
			
			this.addDeployedComponent(c0);
			this.addDeployedComponent(c1);
			this.addDeployedComponent(c2);
			
			c0.start();
			c1.start();
			c2.start();
			
			AdmissionController ac = new AdmissionController(computerPool_uris, ac_uris);
			this.addDeployedComponent(ac);
			ac.start();
			
			super.start();
			
			this.cyclicBarrierClient.waitBarrier();
			
			System.out.println("### DataCenter started !");
			
		} else {
			if (thisJVMURI.equals(DatacenterClient)){
				
				this.cyclicBarrierClient.waitBarrier();
				
				for (int i = 0; i < NB_DATASOURCE; i++) {
					
					HashMap<RGPortTypes, String> rg_uris = RequestGenerator.makeUris(i);
					RequestGenerator rg =new RequestGenerator(rg_uris, new Double(500.0), new Long(6000000000L));
					this.addDeployedComponent(rg);
					rg.start();
					
					AdmissionControllerOutboundPort acop = new AdmissionControllerOutboundPort(new AbstractComponent(0, 0) {});
					
					acop.publishPort();
					
					acop.doConnection(
						ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), 
						ClassFactory.newConnector(AdmissionControllerI.class).getCanonicalName());
					
					acop.createNewRequestDispatcher(i, rg_uris, ac_uris);
					
					RequestGeneratorManagementOutboundPort rgmop = new RequestGeneratorManagementOutboundPort(
						AbstractPort.generatePortURI(),
						new AbstractComponent(0, 0) {});
					
					rgmop.publishPort();
					
					rgmop.doConnection(
						rg_uris.get(RGPortTypes.MANAGEMENT_IN),
						RequestGeneratorManagementConnector.class.getCanonicalName());
					
					super.start();
					
					testScenario(rgmop);
				}
				
				System.out.println("### DataCenterClient started !");
			}
		}
	}
	
	public void testScenario(RequestGeneratorManagementOutboundPort rgmop) throws Exception {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000L);
					rgmop.startGeneration();
					Thread.sleep(20000L);
					rgmop.stopGeneration();
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}).start();
	}
	
	public static void main(String[] args){
		try {
			DistributedTest dTest = new DistributedTest(args);
			dTest.deploy();
			System.out.println("starting...");
			dTest.start();
			Thread.sleep(90000L);
			System.out.println("shutdown...");
			dTest.shutdown();
			System.out.println("ending...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}