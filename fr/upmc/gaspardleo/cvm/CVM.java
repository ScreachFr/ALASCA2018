package fr.upmc.gaspardleo.cvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.hardware.tests.ComputerMonitor;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.cvm.interfaces.CVMI;

public class CVM extends AbstractCVM implements CVMI {
	private final static int NB_CPU = 2;
	private final static int NB_CORES = 2;
	
	//TODO
	// Computer ports
	private static final String ComputerServicesInboundPortURI = "csip";
	private static final String ComputerServicesOutboundPortURI = "csop";
	private static final String ComputerStaticStateDataInboundPortURI = "cssdip";
	private static final String ComputerStaticStateDataOutboundPortURI = "cssdop";
	private static final String ComputerDynamicStateDataInboundPortURI = "cdsdip";
	private static final String ComputerDynamicStateDataOutboundPortURI = "cdsdop";
	
	// Components
	private ComputerMonitor cm;

	// Ports
	private ComputerServicesOutboundPort csPort;
	
	private AllocatedCore[] cores;
	private int currentCore;
	
	private List<ApplicationVMManagementOutboundPort> avmPorts;

	public CVM() throws Exception {
		super();
		this.currentCore = 0;
		this.avmPorts = new ArrayList<>();
	}

	@Override
	public void deploy() throws Exception {
		
		AbstractComponent.configureLogging("", "", 0, '|');
		Processor.DEBUG = true;
		
		// Computer creation
		String computerURI = "computer0";
		int numberOfProcessors = NB_CPU;
		int numberOfCores = NB_CORES;
		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500);	// Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000);	// and at 3 GHz
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000);	// 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000);	// 3 GHz executes 3 Mips
		
		//TODO nouvelle classe extends computer avec enum et getComputerURI
		//TODO Alex
		Computer c = new Computer(
				computerURI,
				admissibleFrequencies,
				processingPower,  
				1500,		// Test scenario 1, frequency = 1,5 GHz
				// 3000,	// Test scenario 2, frequency = 3 GHz
				1500,		// max frequency gap within a processor
				numberOfProcessors,
				numberOfCores,
				ComputerServicesInboundPortURI,
				ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI);
		this.addDeployedComponent(c);

		//TODO pour les ports ne plus utiliser les uri
		
		this.csPort = new ComputerServicesOutboundPort(
				ComputerServicesOutboundPortURI,
				new AbstractComponent(0, 0) {}) ;
		this.csPort.publishPort();
		this.csPort.doConnection(
				ComputerServicesInboundPortURI,
				ComputerServicesConnector.class.getCanonicalName());

		
	
		this.cm = new ComputerMonitor(computerURI,
				true,
				ComputerStaticStateDataOutboundPortURI,
				ComputerDynamicStateDataOutboundPortURI) ;
		this.addDeployedComponent(this.cm) ;

		this.cm.doPortConnection(
				ComputerStaticStateDataOutboundPortURI,
				ComputerStaticStateDataInboundPortURI,
				DataConnector.class.getCanonicalName()) ;

		this.cm.doPortConnection(
				ComputerDynamicStateDataOutboundPortURI,
				ComputerDynamicStateDataInboundPortURI,
				ControlledDataConnector.class.getCanonicalName()) ;
		
		super.deploy();
	}

	public void addAVMPort(ApplicationVMManagementOutboundPort avmPort) {
		this.avmPorts.add(avmPort);
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		
		this.cores = csPort.allocateCores(NB_CPU * NB_CORES);
		
		avmPorts.forEach(avmPort -> {
			try {
				avmPort.allocateCores(getAllocatedCore());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	@Override
	public void deployComponent(ComponentI cmp) throws Exception {
		this.addDeployedComponent(cmp);
	}

	private AllocatedCore[] getAllocatedCore() {
		AllocatedCore[] result = new AllocatedCore[1];
		
		result[0] = cores[currentCore];
		
		currentCore = (currentCore + 1) % cores.length;
		
		return result;
	}
	
	@Override
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		avmPort.allocateCores(getAllocatedCore()) ;
	}
	
}
