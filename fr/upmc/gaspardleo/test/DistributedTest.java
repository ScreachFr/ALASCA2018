package fr.upmc.gaspardleo.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.connectors.AdmissionControllerConnector;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.computer.Computer;
import fr.upmc.gaspardleo.computer.ComputerMonitor;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computer.ComputerMonitor.ComputerMonitorPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class DistributedTest 
	extends	AbstractDistributedCVM{
	
	private static final String Datacenter 			= "datacenter";
	private static final String DatacenterClient 	= "datacenterclient";
	private static String		ac_uri				= "ac_uri";
	
	private Computer 								c;
	private ComputerServicesOutboundPort 			csPort;
	private AllocatedCore[] 						cores;
	private int 									currentCore;
	
	private AdmissionController						ac;
	private AdmissionControllerOutboundPort 		acop;
	
	private RequestGenerator 						rg;
	private RequestGeneratorManagementOutboundPort 	rgmop;
	
	private final static int NB_CPU 				= 2;
	private final static int NB_CORES 				= 2;
	private final static int CPU_FREQUENCY 			= 3000;
	private final static int CPU_MAX_FREQUENCY_GAP 	= 1500;
	
	public DistributedTest(String[] args) throws Exception {
		super(args);
	}
	
	@Override
	public void instantiateAndPublish() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
						
			AbstractComponent.configureLogging("", "", 0, '|');
			Processor.DEBUG = true;
			
			System.out.println("### Creation Computer ...");
			this.c = createComputer();
			this.addDeployedComponent(c);
			Map<ComputerPortsTypes, String> computerPorts = createComputerServicesOutboundPort(c);
			System.out.println("### Computer created");
			System.out.println("");
			
			System.out.println("### Creation Computer Monitor ...");
			ComputerMonitor cm = createComputerMonitor(c);
			connexionComputerMonitorWithComputer(cm,computerPorts);
			System.out.println("### Computer Monitor created");
			System.out.println("");
			
			System.out.println("### Creation AC ...");
			this.ac = new AdmissionController(ac_uri);
			this.addDeployedComponent(ac);
			System.out.println("### AC created");
			System.out.println("");
			
			System.out.println("### Datacenter instantiated and published");
			System.out.println("");
			
		} else {
			if (thisJVMURI.equals(DatacenterClient)){
				
				System.out.println("### Creation RG ...");
				this.rg  = new RequestGenerator("rg");
				this.addDeployedComponent(rg);
				System.out.println("### RG created");
				System.out.println("");
				
				System.out.println("### Creation Management Outbound Port for RG ...");
				this.rgmop = new RequestGeneratorManagementOutboundPort(
					AbstractPort.generatePortURI(),
					new AbstractComponent(0, 0) {});
				this.rgmop.publishPort();
				System.out.println("### Management Outbound Port for RG created");
				System.out.println("");
				
				System.out.println("### Connection RG with his Manager ...");
				this.rgmop.doConnection(
					rg.getRGPortsURI().get(RGPortTypes.MANAGEMENT_IN),
					RequestGeneratorManagementConnector.class.getCanonicalName());
				System.out.println("### RG and his Manager connected");
				System.out.println("");
				
				System.out.println("### DatacenterClient instantiated and published");
				System.out.println("");
				
			} else {
				throw new RuntimeException("unknown JVM " + thisJVMURI);
			}
		}
		super.instantiateAndPublish();
	}
	
	private Computer createComputer() throws Exception{
		
		// Computer creation
		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500);	// Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000);	// and at 3 GHz
		
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000);	// 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000);	// 3 GHz executes 3 Mips
				
		Computer c = new Computer(
				"computer0",
				admissibleFrequencies,
				processingPower,  
				CPU_FREQUENCY,			// Test scenario 1, frequency = 1,5 GHz
				CPU_MAX_FREQUENCY_GAP,	// max frequency gap within a processor
				NB_CPU,
				NB_CORES);
		
		return c;
	}
	
	private Map<ComputerPortsTypes, String> createComputerServicesOutboundPort(Computer c) 
			throws Exception{
		
		Map<ComputerPortsTypes, String> computerPorts = c.getComputerPortsURI();
		
		this.csPort = new ComputerServicesOutboundPort(
				new AbstractComponent(0, 0) {}) ;

		this.csPort.publishPort();
		this.csPort.doConnection(
				computerPorts.get(ComputerPortsTypes.SERVICE_IN),
				ComputerServicesConnector.class.getCanonicalName());
		
		return computerPorts;
	}
	
	private ComputerMonitor createComputerMonitor(Computer c) throws Exception{
		
		ComputerMonitor cm = new ComputerMonitor(
				c.getComputerPortsURI().get(ComputerPortsTypes.INTROSEPTION),true);
		
		return cm;
	}
	
	private void connexionComputerMonitorWithComputer(
			ComputerMonitor cm, Map<ComputerPortsTypes, String> computerPorts) throws Exception{
		
		Map<ComputerMonitorPortTypes, String> computerMonitorPorts = cm.getPortTypes();
		
		cm.doPortConnection(
				computerMonitorPorts.get(ComputerMonitorPortTypes.STATIC_STATE_OUT),
				computerPorts.get(ComputerPortsTypes.STATIC_STATE_IN),
				DataConnector.class.getCanonicalName()) ;

		cm.doPortConnection(
				computerMonitorPorts.get(ComputerMonitorPortTypes.DYNAMIC_STATE_OUT),
				computerPorts.get(ComputerPortsTypes.DYNAMIC_STATE_IN),
				ControlledDataConnector.class.getCanonicalName()) ;
	}
	
	@Override
	public void interconnect() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
			
		} else {
			if (thisJVMURI.equals(DatacenterClient)){
				
//				System.out.println("### Connection RG with AC ...");
//				
//				this.acop = new AdmissionControllerOutboundPort(ac_uri,new AbstractComponent(0, 0) {});
//				acop.publishPort();
//				acop.doConnection(
//						this.rg.getRGPortsURI().get(RGPortTypes.INTROSPECTION),
//						AdmissionControllerConnector.class.getCanonicalName());
//				System.out.println("### RG and AC connected");
//				System.out.println("");
//							
//				System.out.println("### Creation RD ...");
//				RequestDispatcher rd = ((AdmissionControllerI) this.acop).addRequestDispatcher(
//						"rd-",
//						rg.getRGPortsURI().get(RGPortTypes.REQUEST_NOTIFICATION_IN));
//				this.addDeployedComponent(rd);
//				System.out.println("### RD Created");
//				System.out.println("");
//				
//				System.out.println("### Creation AVMs ...");
//				ArrayList<ApplicationVM> vms = this.ac.addApplicationVMs(rd);
//				for (int j = 0; j < vms.size(); j++){
//					this.addDeployedComponent(vms.get(j));
//				}
//				System.out.println("### AVMs Created");
//				System.out.println("");
//				
//				System.out.println("### Connection RG with RD ...");
//				rg.doPortConnection(
//					rg.getRGPortsURI().get(RGPortTypes.REQUEST_SUBMISSION_OUT),
//					rd.getRDPortsURI().get(RDPortTypes.REQUEST_SUBMISSION_IN),
//					RequestSubmissionConnector.class.getCanonicalName());
//				System.out.println("### RG and AC connected");
//				System.out.println("");
				
			} else {
				throw new RuntimeException("unknown JVM " + thisJVMURI);
			}
		}
		super.interconnect();
	}
	
	@Override
	public void start() throws Exception {
				
		if(thisJVMURI.equals(Datacenter)){
						
		} else {
			if (thisJVMURI.equals(DatacenterClient)){
				
				this.cores = this.csPort.allocateCores(NB_CPU * NB_CORES);
				
				ArrayList<ApplicationVMManagementOutboundPort> avmPorts = 
						((AdmissionControllerI) this.acop).getApplicationVMManagementOutboundPorts();
				
				for (int i = 0; i < avmPorts.size(); i++){
					ApplicationVMManagementOutboundPort avmPort = avmPorts.get(i);
						avmPort.allocateCores(getAllocatedCore());
				}
				
			} else {
				throw new RuntimeException("unknown JVM " + thisJVMURI);
			}
		}
		super.start();
	}
	
	private AllocatedCore[] getAllocatedCore() {
		
		AllocatedCore[] result = new AllocatedCore[1];
		
		result[0] = this.cores[this.currentCore];
		
		this.currentCore = (this.currentCore + 1) % this.cores.length;
		
		return result;
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