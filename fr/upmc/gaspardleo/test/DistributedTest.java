package fr.upmc.gaspardleo.test;

import java.util.HashMap;

import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.computer.Computer;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class DistributedTest 
	extends	AbstractDistributedCVM{

	private static final String Datacenter = "datacenter";
	private static final String DatacenterClient = "datacenterclient";
	private static final int NB_DATASOURCE = 1;
	private HashMap<ACPortTypes, String> ac_uris;
		
	public DistributedTest(String[] args) throws Exception {
		super(args);
	}
		
	@Override
	public void start() throws Exception {

		super.start();
		
		ComponentCreator cc = new ComponentCreator(this);
		
		if(thisJVMURI.equals(Datacenter)){
			
			assert cc != null;
			
			HashMap<ComputerPoolPorts, String> cp_uris = 
					ComputerPool.newInstance(cc);
			
			Computer.newInstance("computer-0", cp_uris,	cc);

			ac_uris = AdmissionController.newInstance(cp_uris, cc);
			
			System.out.println("DataCenter started !");
			
		} else {
			if (thisJVMURI.equals(DatacenterClient)){

				for (int i = 0; i < NB_DATASOURCE; i++) {
					
					HashMap<RGPortTypes, String> rg_uris  = 
							RequestGenerator.newInstance("rg-"+i, 500.0, 6000000000L, cc);
					
					RequestDispatcher.newInstance("rd-"+i, rg_uris, ac_uris, cc);
				}
				
				System.out.println("DataCenterClient started !");
			}
		}
	}
	
	public static void testScenario(RequestGeneratorManagementOutboundPort rgmop) throws Exception {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000L);
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
		
		System.out.println();
		System.out.println();
		System.out.println();
		
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