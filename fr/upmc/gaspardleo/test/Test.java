package fr.upmc.gaspardleo.test;

import java.util.HashMap;

import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.computer.Computer;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.cvm.CVM;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class Test {

	private final static int 	NB_DATASOURCE 	= 1;	
	private CVM 				cvm;

	public Test(){
		initTest();
	}

	private void initTest(){
		
		try {
			//TODO
			this.cvm = new CVM();
			
			ComponentCreator cc = new ComponentCreator(cvm);
			
			HashMap<ComputerPoolPorts, String> cp_uris = 
					ComputerPool.newInstance(cc);
			
			Computer.newInstance("computer-0", cp_uris,	cc);
			
			HashMap<ACPortTypes, String> ac_uris = new HashMap<ACPortTypes, String>();		
			ac_uris.put(ACPortTypes.ADMISSION_CONTROLLER_IN, AbstractPort.generatePortURI());
			
			AdmissionController.newInstance(cp_uris, ac_uris, cc);
			
			System.out.println("computer creation launched.");

			for (int i = 0; i < NB_DATASOURCE; i++) {
				
				HashMap<RGPortTypes, String> rg_uris  = 
						RequestGenerator.newInstance("rg-"+i, 500.0, 6000000000L, cc);
				
				RequestDispatcher.newInstance("rd-"+i, rg_uris, ac_uris, cc);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//TODO proposer un scénario qui permet de mettre en évidence le refus de requêtes
	
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
	
	public CVM getCvm() {
		return cvm;
	}

	public static void main(String[] args){

		try {
			final Test tvmc = new Test() ;
			System.out.println("starting...") ;
			tvmc.getCvm().start() ;
			Thread.sleep(90000L) ;
			System.out.println("shutting down...") ;
			tvmc.getCvm().shutdown() ;
			System.out.println("ending...") ;
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}