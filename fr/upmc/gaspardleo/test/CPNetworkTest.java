package fr.upmc.gaspardleo.test;

import java.util.HashMap;
import java.util.HashSet;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionControllerPoolNetwork;
import fr.upmc.gaspardleo.computer.Computer;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.ComputerPoolNetworkMaster;
import fr.upmc.gaspardleo.cvm.CVM;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class CPNetworkTest {

	private final static int NB_DATASOURCE 	= 1;	
	private CVM cvm;

	public CPNetworkTest(){
		initTest();
	}

	private void initTest(){
		
		try {
			this.cvm = new CVM();
			
			HashMap<ComputerPoolPorts, String> cp_uris_1 = ComputerPool.makeUris();
			new ComputerPool(cp_uris_1);
			HashMap<ComputerPoolPorts, String> cp_uris_2 = ComputerPool.makeUris();
			new ComputerPool(cp_uris_2);
			HashMap<ComputerPoolPorts, String> cp_uris_3 = ComputerPool.makeUris();
			new ComputerPool(cp_uris_3);
			
			
			
			
			HashSet<Integer> admissibleFrequencies = Computer.makeFrequencies();
			HashMap<Integer,Integer> processingPower = Computer.makeProcessingPower();
			new Computer(Computer.makeUris(0), cp_uris_1, admissibleFrequencies, processingPower);
			new Computer(Computer.makeUris(1), cp_uris_2, admissibleFrequencies, processingPower);
			new Computer(Computer.makeUris(2), cp_uris_3, admissibleFrequencies, processingPower);

			String cpnm_in = AbstractPort.generatePortURI();
			ComputerPoolNetworkMaster cpnm = new ComputerPoolNetworkMaster("cpnm", cpnm_in);

			cpnm.registerComputerPool(cp_uris_1.get(ComputerPoolPorts.INTROSPECTION), cp_uris_1.get(ComputerPoolPorts.COMPUTER_POOL));
			cpnm.registerComputerPool(cp_uris_2.get(ComputerPoolPorts.INTROSPECTION), cp_uris_2.get(ComputerPoolPorts.COMPUTER_POOL));
			cpnm.registerComputerPool(cp_uris_3.get(ComputerPoolPorts.INTROSPECTION), cp_uris_3.get(ComputerPoolPorts.COMPUTER_POOL));
			
			HashMap<ACPortTypes, String> ac_uris = AdmissionController.makeUris("AC_URI");
			new AdmissionControllerPoolNetwork(cpnm_in, ac_uris);
			
			for (int i = 0; i < NB_DATASOURCE; i++) {
				
				HashMap<RGPortTypes, String> rg_uris = RequestGenerator.makeUris(i);
				new RequestGenerator(rg_uris, 500.0, 6000000000L);
				
				new RequestDispatcher(RequestDispatcher.makeUris(i), rg_uris, ac_uris);
				
				RequestGeneratorManagementOutboundPort rgmop = new RequestGeneratorManagementOutboundPort(
					AbstractPort.generatePortURI(),
					new AbstractComponent(0, 0) {});
				
				rgmop.publishPort();
				
				rgmop.doConnection(
					rg_uris.get(RGPortTypes.MANAGEMENT_IN),
					RequestGeneratorManagementConnector.class.getCanonicalName());
				
				testScenario(rgmop);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public CVM getCvm() {
		return cvm;
	}
	
	public void testScenario(RequestGeneratorManagementOutboundPort rgmop) throws Exception {
		
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

		try {
			final CPNetworkTest tvmc = new CPNetworkTest() ;
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