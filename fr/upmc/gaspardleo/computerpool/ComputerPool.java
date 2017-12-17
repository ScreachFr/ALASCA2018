package fr.upmc.gaspardleo.computerpool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreator;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computer.Computer;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.exceptions.NoAvailableResourceException;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolInbounPort;

public class ComputerPool extends AbstractComponent implements ComputerPoolI {
	public final static int DEFAULT_CORE_ALLOC_NUMBER = 2;

	private String uri;
	
	private DynamicComponentCreator dcc;

	private ComputerPoolInbounPort cpi;
	
	
	private List<Computer> computers;
	private List<AllocatedCore[]> availableCores;
	private Map<Map<ApplicationVMPortTypes, String>, AllocatedCore[]> avmInUse;

	public ComputerPool(String componentURI, String ComputerPoolPort_IN, DynamicComponentCreator dcc) throws Exception {
		super(1, 1);

		this.uri = componentURI;

		
		this.addOfferedInterface(ComputerPoolI.class);
		this.cpi = new ComputerPoolInbounPort(ComputerPoolPort_IN, this);
		this.cpi.publishPort();
		this.addPort(this.cpi);
		
		

		this.computers = new ArrayList<>();
		this.availableCores = new ArrayList<>();
		this.avmInUse = new HashMap<>();
	}

	@Override
	public void createNewComputer(String computerURI, Set<Integer> possibleFrequencies,
			Map<Integer, Integer> processingPower, int defaultFrequency, int maxFrequencyGap, int numberOfProcessors,
			int numberOfCores) throws Exception {

		System.out.println("Computer creation and core allocation.");
		
		Map<ComputerPortsTypes, String> computerUris = Computer.newInstance(computerURI, possibleFrequencies, 
				processingPower, defaultFrequency, maxFrequencyGap, numberOfProcessors, numberOfCores, dcc);

		ComputerServicesOutboundPort csop = new ComputerServicesOutboundPort(this);
		this.addPort(csop);
		csop.publishPort();
		csop.doConnection(computerUris.get(ComputerPortsTypes.SERVICE_IN), ComputerServicesConnector.class.getCanonicalName());
		
		for (int i = 0; i < (numberOfProcessors * numberOfCores)/2; i++) {
			availableCores.add(csop.allocateCores(DEFAULT_CORE_ALLOC_NUMBER));
		}

	}

	@Override
	public Map<ApplicationVMPortTypes, String> createNewApplicationVM(String avmURI, int numberOfCoreToAllocate)  throws Exception{
		// Pas de core sous la main.
		if (availableCores.size() == 0)
			throw new NoAvailableResourceException();

		Map<ApplicationVMPortTypes, String> result = ApplicationVM.newInstance(avmURI, dcc);
		
		// Create a mock up port to manage the AVM component (allocate cores).
		ApplicationVMManagementOutboundPort avmPort = new ApplicationVMManagementOutboundPort(
				new AbstractComponent(0, 0) {});
		avmPort.publishPort();

		avmPort.doConnection(
				result.get(ApplicationVMPortTypes.MANAGEMENT),
				ApplicationVMManagementConnector.class.getCanonicalName());
		
		
		
		AllocatedCore[] cores = availableCores.remove(0);
		avmPort.allocateCores(cores);
		
		avmInUse.put(result, cores);
		
		
		return result;
	}
	
	// TODO pouvoir enlever des avms et rendre les cores de nouveau disponible.
	
	public static Map<ComputerPoolPorts, String> newInstance(String componentURI, DynamicComponentCreator dcc) throws Exception {
		String computerPoolPort_URI = AbstractPort.generatePortURI();
		
		
		dcc.createComponent(ComputerPool.class.getCanonicalName(), new Object[]{componentURI, computerPoolPort_URI, dcc});
		
		
		HashMap<ComputerPoolPorts, String> result = new HashMap<>();
		result.put(ComputerPoolPorts.COMPUTER_POOL, computerPoolPort_URI);
		
		
		return result;
	}

	public enum ComputerPoolPorts {
		COMPUTER_POOL;
	}
}
