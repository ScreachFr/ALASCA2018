package fr.upmc.gaspardleo.test;

import java.util.HashMap;
import java.util.HashSet;

import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.computer.Computer;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class DistributedTest 
	extends	AbstractDistributedCVM{

	private static final String Datacenter = "datacenter";
	private static final String DatacenterClient = "datacenterclient";
	private static final int NB_DATASOURCE = 1;
	private static final String AC_URI = "AC_URI";
	private HashMap<ACPortTypes, String> ac_uris;
	private ComponentCreator cc;
	
	public DistributedTest(String[] args) throws Exception {
		super(args);
		
		ac_uris = new HashMap<>();
		ac_uris.put(ACPortTypes.ADMISSION_CONTROLLER_IN, AC_URI);
	}
		
	@Override
	public void start() throws Exception {

		super.start();
		
		cc = new ComponentCreator(this);
				
		if(thisJVMURI.equals(Datacenter)){
						
			HashMap<ComputerPoolPorts, String> computerPool_uris = new HashMap<>();
			computerPool_uris.put(ComputerPoolPorts.COMPUTER_POOL, AbstractPort.generatePortURI());
			computerPool_uris.put(ComputerPoolPorts.INTROSPECTION, AbstractPort.generatePortURI());
			
			ComputerPool cpp = new ComputerPool(computerPool_uris, cc);
			this.addDeployedComponent(cpp);
			cpp.start();
			
			HashSet<Integer> admissibleFrequencies = new HashSet<Integer>() ;
			admissibleFrequencies.add(1500);
			admissibleFrequencies.add(3000);
			
			HashMap<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
			processingPower.put(1500, 1500000);
			processingPower.put(3000, 3000000);
			
			HashMap<ComputerPortsTypes, String> computer_uris_0 = new HashMap<>();
			computer_uris_0.put(ComputerPortsTypes.INTROSPECTION, "computer-0");
			computer_uris_0.put(ComputerPortsTypes.SERVICE_IN, AbstractPort.generatePortURI());
			computer_uris_0.put(ComputerPortsTypes.STATIC_STATE_IN, AbstractPort.generatePortURI());
			computer_uris_0.put(ComputerPortsTypes.DYNAMIC_STATE_IN, AbstractPort.generatePortURI());
			
			Computer computer_0 = new Computer(computer_uris_0, computerPool_uris, admissibleFrequencies, processingPower);
			this.addDeployedComponent(computer_0);
			computer_0.start();
			
			HashMap<ComputerPortsTypes, String> computer_uris_1 = new HashMap<>();
			computer_uris_1.put(ComputerPortsTypes.INTROSPECTION, "computer-1");
			computer_uris_1.put(ComputerPortsTypes.SERVICE_IN, AbstractPort.generatePortURI());
			computer_uris_1.put(ComputerPortsTypes.STATIC_STATE_IN, AbstractPort.generatePortURI());
			computer_uris_1.put(ComputerPortsTypes.DYNAMIC_STATE_IN, AbstractPort.generatePortURI());
			
			Computer computer_1 = new Computer(computer_uris_1, computerPool_uris, admissibleFrequencies, processingPower);
			this.addDeployedComponent(computer_1);
			computer_1.start();
			
			HashMap<ComputerPortsTypes, String> computer_uris_2 = new HashMap<>();
			computer_uris_2.put(ComputerPortsTypes.INTROSPECTION, "computer-2");
			computer_uris_2.put(ComputerPortsTypes.SERVICE_IN, AbstractPort.generatePortURI());
			computer_uris_2.put(ComputerPortsTypes.STATIC_STATE_IN, AbstractPort.generatePortURI());
			computer_uris_2.put(ComputerPortsTypes.DYNAMIC_STATE_IN, AbstractPort.generatePortURI());
			
			Computer computer_2 = new Computer(computer_uris_2, computerPool_uris, admissibleFrequencies, processingPower);
			this.addDeployedComponent(computer_2);
			computer_2.start();
			
			System.out.println("computers creation launched.");
						
			AdmissionController ac = new AdmissionController(computerPool_uris, ac_uris, cc);
			this.addDeployedComponent(ac);
			ac.start();
			
			for (int i = 0; i < NB_DATASOURCE; i++) {
				
				HashMap<RGPortTypes, String> rg_uris = new HashMap<RGPortTypes, String>();		
				rg_uris.put(RGPortTypes.INTROSPECTION, "rg-"+i);
				rg_uris.put(RGPortTypes.CONNECTION_IN, AbstractPort.generatePortURI());
				rg_uris.put(RGPortTypes.MANAGEMENT_IN, AbstractPort.generatePortURI()) ;
				rg_uris.put(RGPortTypes.REQUEST_SUBMISSION_OUT, AbstractPort.generatePortURI());
				rg_uris.put(RGPortTypes.REQUEST_NOTIFICATION_IN, AbstractPort.generatePortURI());
				
				RequestGenerator rg = new RequestGenerator(rg_uris, new Double(500.0), new Long(6000000000L));
				this.addDeployedComponent(rg);
				rg.start();
				
				String requestMonitor_in = AbstractPort.generatePortURI();
				
				HashMap<RDPortTypes, String> requestDispatcher_uris = new HashMap<RDPortTypes, String>() ;		
				requestDispatcher_uris.put(RDPortTypes.INTROSPECTION, "rd-"+i);
				requestDispatcher_uris.put(RDPortTypes.REQUEST_SUBMISSION_IN, AbstractPort.generatePortURI());
				requestDispatcher_uris.put(RDPortTypes.REQUEST_SUBMISSION_OUT, AbstractPort.generatePortURI());
				requestDispatcher_uris.put(RDPortTypes.REQUEST_NOTIFICATION_IN, AbstractPort.generatePortURI());
				requestDispatcher_uris.put(RDPortTypes.REQUEST_NOTIFICATION_OUT, AbstractPort.generatePortURI());
				requestDispatcher_uris.put(RDPortTypes.REQUEST_DISPATCHER_IN, AbstractPort.generatePortURI());
				requestDispatcher_uris.put(RDPortTypes.SHUTDOWNABLE_IN, AbstractPort.generatePortURI());
				requestDispatcher_uris.put(RDPortTypes.REQUEST_GENERATOR_MANAGER_OUT, AbstractPort.generatePortURI());
				
				RequestDispatcher rd = new RequestDispatcher(requestDispatcher_uris, rg_uris, ac_uris, requestMonitor_in);
				this.addDeployedComponent(rd);
				rd.start();
			}
			
			System.out.println("### DataCenter started !");
			
		} else {
			if (thisJVMURI.equals(DatacenterClient)){

				
				System.out.println("### DataCenterClient started !");
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