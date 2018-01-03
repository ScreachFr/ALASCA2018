package fr.upmc.gaspardleo.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.extensions.synchronizers.components.DistributedSynchronizerManager;
import fr.upmc.components.extensions.synchronizers.components.SynchronizerManager;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.connectors.AdmissionControllerConnector;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.connectors.ComputerPoolConnector;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;
import fr.upmc.gaspardleo.cvm.CVM;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class Test {

	private final static int NB_CPU 				= 2;
	private final static int NB_CORES 				= 4;
	private final static int CPU_FREQUENCY 			= 3000;
	private final static int CPU_MAX_FREQUENCY_GAP = 1500;

	private final static int 	NB_DATASOURCE 	= 1;	
	private final static String AC_URI 			= "AC_URI";
	private final static String CP_URI			= "CP_URI";

	private CVM 				cvm;
	//	private AdmissionController	ac;

	private List<RequestGeneratorManagementOutboundPort> rgmops;

	private Map<ACPortTypes, String> ac_uris;
	private Map<ComputerPoolPorts, String> cp_uris;

	public Test(){
		rgmops = new ArrayList<>();
		initTest();
	}

	private void initTest(){
		try {


			//TODO
			// CVM creation
			this.cvm 	= new CVM();
			
			ComponentCreator cc = new ComponentCreator();
			assert cc != null : "cc is null";
			this.cvm.addDeployedComponent(cc);
			
			cp_uris = ComputerPool.newInstance(cc);

			ComputerPoolOutboundPort cpop = new ComputerPoolOutboundPort(new AbstractComponent(0, 0) {});
			cpop.publishPort();
			
			//BUG avec Javassist en Multi-JVM
//		cpop.doConnection(cp_uris.get(ComputerPoolPorts.COMPUTER_POOL),
//				ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());
			
			cpop.doConnection(cp_uris.get(ComputerPoolPorts.COMPUTER_POOL),
					ComputerPoolConnector.class.getCanonicalName());

			// Computer creation
			HashSet<Integer> admissibleFrequencies = new HashSet<Integer>() ;
			admissibleFrequencies.add(1500);	// Cores can run at 1,5 GHz
			admissibleFrequencies.add(3000);	// and at 3 GHz

			HashMap<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
			processingPower.put(1500, 1500000);	// 1,5 GHz executes 1,5 Mips
			processingPower.put(3000, 3000000); // 3 GHz executes 3 Mips

			cpop.createNewComputer("computer-0",
				admissibleFrequencies,
				processingPower,
				CPU_FREQUENCY,
				CPU_MAX_FREQUENCY_GAP,
				NB_CPU,
				NB_CORES,
				cc);
			
			System.out.println("computer creation launched.");
			
			// Admission Controller creation
			ac_uris = AdmissionController.newInstance(cp_uris, cc);

			// Simply adds some request generators to the current admission controller.
			for (int i = 0; i < NB_DATASOURCE; i++) {
				// Request Generator creation
				Map<RGPortTypes, String> rg_uris  = RequestGenerator.newInstance("rg-"+i, 500.0, 6000000000L, cc);
				
				// Rg management creation
				RequestGeneratorManagementOutboundPort rgmop = new RequestGeneratorManagementOutboundPort(
						new AbstractComponent(0, 0) {});
				
				rgmop.publishPort();
				
				rgmop.doConnection(
						rg_uris.get(RGPortTypes.MANAGEMENT_IN),
						RequestGeneratorManagementConnector.class.getCanonicalName());
				
				List<RequestGeneratorManagementOutboundPort> rgmops = new ArrayList<>();
				rgmops.add(rgmop);
				
				// Dynamic ressources creation
				AdmissionControllerOutboundPort acop = new AdmissionControllerOutboundPort(
						new AbstractComponent(0, 0) {});
				
				acop.publishPort();
				
				acop.doConnection(
						ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), 
						AdmissionControllerConnector.class.getCanonicalName());
				
				acop.addRequestDispatcher("rd-"+i, rg_uris, cc);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void addDataSource(int i, SynchronizerManager sm) throws Exception {
//
//		// Request Generator creation
//		Map<RGPortTypes, String> rg  = createRequestGenerator("rg-"+i, sm);
//
//		// Dynamic ressources creation
//		AdmissionControllerOutboundPort acop = new AdmissionControllerOutboundPort(AbstractPort.generatePortURI(), new AbstractComponent(0, 0) {});
//		acop.publishPort();
//		acop.doConnection(ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), AdmissionControllerConnector.class.getCanonicalName());
//		acop.addRequestDispatcher("rd-"+i, rg);
//	}
//
//	private Map<RGPortTypes, String> createRequestGenerator(String RG_URI, SynchronizerManager sm) throws Exception{
//		Map<RGPortTypes, String> result = RequestGenerator.newInstance(RG_URI, 500.0, 6000000000L, sm, false);
//
//		createRGManagement(result.get(RGPortTypes.MANAGEMENT_IN));
//
//
//		return result;
//	}
//
//	private void createRGManagement(String rg_management_in) throws Exception{
//
//		// Rg management creation
//		RequestGeneratorManagementOutboundPort rgmop = new RequestGeneratorManagementOutboundPort(
//				AbstractPort.generatePortURI(),
//				new AbstractComponent(0, 0) {});
//
//		rgmop.publishPort();
//
//		rgmop.doConnection(
//				rg_management_in,
//				RequestGeneratorManagementConnector.class.getCanonicalName());
//
//		this.rgmops.add(rgmop);
//	}

	//TODO proposer un scénario qui permet de mettre en évidence le refus de requêtes

	public void testScenario() throws Exception {

		// start the request generation in the request generator.

		
		Thread.sleep(1000L);
		for(int i = 0; i < this.rgmops.size(); i++){
			RequestGeneratorManagementOutboundPort rgmop = this.rgmops.get(i);
			rgmop.startGeneration();
		}

		// wait 20 seconds
		Thread.sleep(20000L);
		// then stop the generation.

		for(int i = 0; i < this.rgmops.size(); i++){
			RequestGeneratorManagementOutboundPort rgmop = this.rgmops.get(i);
			rgmop.stopGeneration();
		}

	}

	public CVM getCvm() {
		return cvm;
	}

	public static void main(String[] args){

		// AbstractCVM.toggleDebugMode() ;
		try {
			final Test tvmc = new Test() ;
			// Deploy the components
			//			tvmc.getCvm().deploy() ;

			System.out.println("starting...") ;
			// Start them.
			tvmc.getCvm().start() ;

			// Execute the chosen request generation test scenario in a
			// separate thread.
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						tvmc.testScenario();
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}).start();

			// Sleep to let the test scenario execute to completion.
			Thread.sleep(90000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
			tvmc.getCvm().shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}