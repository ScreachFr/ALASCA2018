package fr.upmc.gaspardleo.cvm;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public class CVM extends AbstractCVM {
	
//	private final static int NB_CPU 				= 2;
//	private final static int NB_CORES 				= 2;
//	private final static int CPU_FREQUENCY 			= 3000;
//	private final static int CPU_MAX_FREQUENCY_GAP 	= 1500;
//	private ComputerServicesOutboundPort 				csPort;	// Ports
//	private List<ApplicationVMManagementOutboundPort> 	avmPorts;
	
	private AllocatedCore[] 							cores;
	private int 										currentCore;
	
	public CVM() throws Exception {		
		super();
				
		this.currentCore = 0;
//		this.avmPorts = new ArrayList<>();
		
	}

	@Override
	public void deploy() throws Exception {
		
		AbstractComponent.configureLogging("", "", 0, '|');
		Processor.DEBUG = true;
		
//		Computer c = createComputer();
//		Map<ComputerPortsTypes, String> computerPorts = createComputerServicesOutboundPort(c);
		
//		ComputerMonitor cm = createComputerMonitor(c);
//		connexionComputerMonitorWithComputer(cm,computerPorts);
		
		super.deploy();
	}
	
//	private Computer createComputer() throws Exception{
//		System.out.println("Computer creation and core allocation.");
//		
//		// Computer creation
//		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
//		admissibleFrequencies.add(1500);	// Cores can run at 1,5 GHz
//		admissibleFrequencies.add(3000);	// and at 3 GHz
//		
//		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
//		processingPower.put(1500, 1500000);	// 1,5 GHz executes 1,5 Mips
//		processingPower.put(3000, 3000000);	// 3 GHz executes 3 Mips
//				
//		Computer c = new Computer(
//				"computer0",
//				admissibleFrequencies,
//				processingPower,  
//				CPU_FREQUENCY,			// Test scenario 1, frequency = 1,5 GHz
//				CPU_MAX_FREQUENCY_GAP,	// max frequency gap within a processor
//				NB_CPU,
//				NB_CORES);
//		
//		this.addDeployedComponent(c);
//				
//		cores = c.allocateCores(NB_CPU * NB_CORES);
//				
//		return c;
//	}
//	
//	private Map<ComputerPortsTypes, String> createComputerServicesOutboundPort(Computer c) 
//			throws Exception{
//		
//		Map<ComputerPortsTypes, String> computerPorts = c.getComputerPortsURI();
//		
//		this.csPort = new ComputerServicesOutboundPort(
//				new AbstractComponent(0, 0) {}) ;
//
//		this.csPort.publishPort();
//		this.csPort.doConnection(
//				computerPorts.get(ComputerPortsTypes.SERVICE_IN),
//				ComputerServicesConnector.class.getCanonicalName());
//		
//		return computerPorts;
//	}
//	
//	private ComputerMonitor createComputerMonitor(Computer c) throws Exception{
//		
//		ComputerMonitor cm = new ComputerMonitor(
//				c.getComputerPortsURI().get(ComputerPortsTypes.INTROSEPTION),true);
//		
//		this.addDeployedComponent(cm);
//		
//		return cm;
//	}
//	
//	private void connexionComputerMonitorWithComputer(
//			ComputerMonitor cm, Map<ComputerPortsTypes, String> computerPorts) throws Exception{
//		
//		Map<ComputerMonitorPortTypes, String> computerMonitorPorts = cm.getPortTypes();
//		
//		cm.doPortConnection(
//				computerMonitorPorts.get(ComputerMonitorPortTypes.STATIC_STATE_OUT),
//				computerPorts.get(ComputerPortsTypes.STATIC_STATE_IN),
//				DataConnector.class.getCanonicalName()) ;
//
//		cm.doPortConnection(
//				computerMonitorPorts.get(ComputerMonitorPortTypes.DYNAMIC_STATE_OUT),
//				computerPorts.get(ComputerPortsTypes.DYNAMIC_STATE_IN),
//				ControlledDataConnector.class.getCanonicalName()) ;
//	}
//
//	@Override
//	public void addAVMPort(ApplicationVMManagementOutboundPort avmPort) {
//		
//		this.avmPorts.add(avmPort);
//	}
//	
	@Override
	public void start() throws Exception {
		
		super.start();
		
//		this.cores = this.csPort.allocateCores(NB_CPU * NB_CORES);
//		
//		for (int i = 0; i < this.avmPorts.size(); i++){
//			ApplicationVMManagementOutboundPort avmPort = this.avmPorts.get(i);
//				avmPort.allocateCores(getAllocatedCore());
//		}
	}

	private AllocatedCore[] getAllocatedCore() {
		
		AllocatedCore[] result = new AllocatedCore[1];
		
		result[0] = this.cores[this.currentCore];
		
		this.currentCore = (this.currentCore + 1) % this.cores.length;
				
		return result;
	}
	
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		
		avmPort.allocateCores(getAllocatedCore()) ;
	}
}
