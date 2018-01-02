package fr.upmc.gaspardleo.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.extensions.synchronizers.components.DistributedSynchronizerManager;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.connectors.AdmissionControllerConnector;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.connectors.ComputerPoolConnector;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class DistributedTest 
	extends	AbstractDistributedCVM{
	
	//DataCenter
	
	private static final String Datacenter = "datacenter";
	private static final String CP_URI = "CP_URI";
	private static final String AC_URI = "AC_URI";

	private static final int CPU_FREQUENCY = 3000;
	private static final int CPU_MAX_FREQUENCY_GAP = 1500;
	private static final int NB_CPU = 2;
	private static final int NB_CORES = 4;

//	private DynamicComponentCreationOutboundPort dccop_1;
	
	private Map<ComputerPoolPorts, String> cp_uris;
	private Map<ACPortTypes, String> ac_uris;
	
	//DataCenterClient
	
	private static final String DatacenterClient = "datacenterclient";
	private static final int 	NB_DATASOURCE 	= 1;	
	
//	private DynamicComponentCreationOutboundPort dccop_2;

	private List<RequestGeneratorManagementOutboundPort> rgmops;

	
	private DistributedSynchronizerManager dsm_1;
	private DistributedSynchronizerManager dsm_2;

	protected final String	synchronizerManagerURI_1 = "sm_uri_1";
	protected final String	synchronizerManagerURI_2 = "sm_uri_2";
	
	public DistributedTest(String[] args) throws Exception {
		super(args);
	}
		
	@Override
	public void instantiateAndPublish() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
			
//			dccop_1 = new DynamicComponentCreationOutboundPort(new AbstractComponent(0, 0) {});
//			dccop_1.publishPort();
//			
			
			dsm_1 = new DistributedSynchronizerManager(
					synchronizerManagerURI_1, true);
			this.deployedComponents.add(dsm_1);
			
		} else {
			if (thisJVMURI.equals(DatacenterClient)){
				
//				dccop_2 = new DynamicComponentCreationOutboundPort(new AbstractComponent(0, 0) {});
//				dccop_2.publishPort();
				
				dsm_2 = new DistributedSynchronizerManager(
						synchronizerManagerURI_2, true);
				this.deployedComponents.add(dsm_2);
			}
		}
		
		System.out.println("[DEBUG LEO] instantiateAndPublish ok");
		
		super.instantiateAndPublish();
	}
	
	@Override
	public void interconnect() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
						
//			assert dccop_1 != null;
//			
//			dccop_1.doConnection(thisJVMURI + AbstractDistributedCVM.DCC_INBOUNDPORT_URI_SUFFIX, 
//					DynamicComponentCreationConnector.class.getCanonicalName());
						
		} else {
			if (thisJVMURI.equals(DatacenterClient)){	
				
//				assert dccop_2 != null;
//				
//				dccop_2.doConnection(thisJVMURI + AbstractDistributedCVM.DCC_INBOUNDPORT_URI_SUFFIX, 
//						DynamicComponentCreationConnector.class.getCanonicalName());
			}
		}
		
		super.interconnect();
	}
	
	@Override
	public void start() throws Exception {

		super.start();
		
		if(thisJVMURI.equals(Datacenter)){
			
			assert dsm_1 != null;
						
			cp_uris = ComputerPool.newInstance(CP_URI, dsm_1, true);

			System.out.println("[DEBUG LEO] cp_uris length : " + cp_uris.size());
			for(ComputerPoolPorts k : cp_uris.keySet()){
				System.out.println("[DEBUG LEO] key : " + k + "; value : " + cp_uris.get(k));
			}

			ComputerPoolOutboundPort cpop = new ComputerPoolOutboundPort(new AbstractComponent(0, 0) {});
			cpop.publishPort();
			
			//BUG avec Javassist en Multi-JVM
//			cpop.doConnection(cp_uris.get(ComputerPoolPorts.COMPUTER_POOL),
//					ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());
			
			cpop.doConnection(cp_uris.get(ComputerPoolPorts.COMPUTER_POOL),
					ComputerPoolConnector.class.getCanonicalName());
//
//			HashSet<Integer> admissibleFrequencies = new HashSet<Integer>() ;
//			admissibleFrequencies.add(1500);
//			admissibleFrequencies.add(3000);
//
//			HashMap<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
//			processingPower.put(1500, 1500000);
//			processingPower.put(3000, 3000000);
//
//			cpop.createNewComputer("computer-0",
//					admissibleFrequencies,
//					processingPower,
//					CPU_FREQUENCY,
//					CPU_MAX_FREQUENCY_GAP,
//					NB_CPU,
//					NB_CORES);
//
//			ac_uris = AdmissionController.newInstance(AC_URI, cp_uris, null, dsm_1, true);
//
			System.out.println("DataCenter started !");
			
		} else {
			if (thisJVMURI.equals(DatacenterClient)){

//				assert dsm_2 != null;
//				
//				for (int i = 0; i < NB_DATASOURCE; i++) {
//					
//					// Request Generator creation
//					Map<RGPortTypes, String> rg_uris  = RequestGenerator.newInstance("rg-"+i, 500.0, 6000000000L, null, dsm_2, true);
//					
//					// Rg management creation
//					RequestGeneratorManagementOutboundPort rgmop = new RequestGeneratorManagementOutboundPort(
//							new AbstractComponent(0, 0) {});
//					rgmop.publishPort();
//					rgmop.doConnection(
//							rg_uris.get(RGPortTypes.MANAGEMENT_IN),
//							RequestGeneratorManagementConnector.class.getCanonicalName());
//					this.rgmops.add(rgmop);
//					
//					// Dynamic ressources creation
//					AdmissionControllerOutboundPort acop = new AdmissionControllerOutboundPort(
//							new AbstractComponent(0, 0) {});
//					acop.publishPort();
//					acop.doConnection(
//							ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), 
//							AdmissionControllerConnector.class.getCanonicalName());
//					acop.addRequestDispatcher("rd-"+i, rg_uris);				
//				}
				System.out.println("DataCenterClient started !");
			}
		}
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
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						//dTest.testScenario();
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}).start();
			Thread.sleep(90000L);
			System.out.println("shutdown...");
			dTest.shutdown();
			System.out.println("ending...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}