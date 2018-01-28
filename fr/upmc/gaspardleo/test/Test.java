package fr.upmc.gaspardleo.test;

import java.util.HashMap;
import java.util.HashSet;

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
			this.cvm = new CVM();
			
			ComponentCreator cc = new ComponentCreator(cvm);
			
			HashMap<ComputerPoolPorts, String> computerPool_uris = ComputerPool.makeUris();
			new ComputerPool(computerPool_uris, cc);
			
			HashSet<Integer> admissibleFrequencies = Computer.makeFrequencies();
			HashMap<Integer,Integer> processingPower = Computer.makeProcessingPower();
			new Computer(Computer.makeUris(0), computerPool_uris, admissibleFrequencies, processingPower);
			new Computer(Computer.makeUris(1), computerPool_uris, admissibleFrequencies, processingPower);
			new Computer(Computer.makeUris(2), computerPool_uris, admissibleFrequencies, processingPower);

			HashMap<ACPortTypes, String> ac_uris = AdmissionController.makeUris("AC_URI");
			new AdmissionController(computerPool_uris, ac_uris, cc);
			
			for (int i = 0; i < NB_DATASOURCE; i++) {
				
				HashMap<RGPortTypes, String> rg_uris = RequestGenerator.makeUris(i);
				new RequestGenerator(rg_uris, new Double(500.0), new Long(6000000000L));
				
				new RequestDispatcher(RequestDispatcher.makeUris(i), rg_uris, ac_uris);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
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