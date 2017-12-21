package fr.upmc.gaspardleo.computerpool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
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

	public enum ComputerPoolPorts {
		ITROSPECTION,
		COMPUTER_POOL;
	}
	
	private String uri;
	
	private DynamicComponentCreationOutboundPort dcc;

	private ComputerPoolInbounPort cpi;
	
	private List<Map<ComputerPortsTypes, String>> computers;
	
	private List<AllocatedCore[]> availableCores;
	private Map<Map<ApplicationVMPortTypes, String>, AllocatedCore[]> avmInUse;

	public ComputerPool(String componentURI, String ComputerPoolPort_IN, DynamicComponentCreationOutboundPort dcc) throws Exception {
		super(1, 1);

		this.uri = componentURI;

		
		this.addOfferedInterface(ComputerPoolI.class);
		this.cpi = new ComputerPoolInbounPort(ComputerPoolPort_IN, this);
		this.cpi.publishPort();
		this.addPort(this.cpi);
		
		this.dcc = dcc;

		this.computers = new ArrayList<>();
		this.availableCores = new ArrayList<>();
		this.avmInUse = new HashMap<>();
		
		this.toggleLogging();
		this.toggleTracing();
	}

	@Override
	public void createNewComputer(String computerURI,
			HashSet<Integer> possibleFrequencies,
			HashMap<Integer, Integer> processingPower,
			Integer defaultFrequency,
			Integer maxFrequencyGap,
			Integer numberOfProcessors,
			Integer numberOfCores) throws Exception {

		this.logMessage("Computer Pool : Computer creation and core allocation.");
		
		Map<ComputerPortsTypes, String> computerUris = Computer.newInstance(computerURI, possibleFrequencies, 
				processingPower, defaultFrequency, maxFrequencyGap, numberOfProcessors, numberOfCores, dcc);

		ComputerServicesOutboundPort csop = new ComputerServicesOutboundPort(this);
		this.addPort(csop);
		csop.publishPort();
		csop.doConnection(computerUris.get(ComputerPortsTypes.SERVICE_IN), ComputerServicesConnector.class.getCanonicalName());
		
		for (int i = 0; i < (numberOfProcessors * numberOfCores)/2; i++) {
			availableCores.add(csop.allocateCores(DEFAULT_CORE_ALLOC_NUMBER));
		}
		
		this.computers.add(computerUris);
	}

	@Override
	public Map<ApplicationVMPortTypes, String> createNewApplicationVM(String avmURI, Integer numberOfCoreToAllocate)  throws Exception{
		// Pas de core sous la main.
		if (availableCores.size() == 0)
			throw new NoAvailableResourceException();

		Map<ApplicationVMPortTypes, String> result = ApplicationVM.newInstance(dcc, avmURI);
		
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
	
	public String getUri() {
		return uri;
	}
	
	// TODO pouvoir enlever des avms et rendre les cores de nouveau disponible.
	
	public static Map<ComputerPoolPorts, String> newInstance(String componentURI, DynamicComponentCreationOutboundPort dcc) throws Exception {
		String computerPoolPort_URI = AbstractPort.generatePortURI();
		
		Object[] args = new Object[]{
				componentURI,
				computerPoolPort_URI,
				dcc
		};
		
		dcc.createComponent(ComputerPool.class.getCanonicalName(), args);
		
		
		HashMap<ComputerPoolPorts, String> result = new HashMap<>();
		
		result.put(ComputerPoolPorts.COMPUTER_POOL, computerPoolPort_URI);
		result.put(ComputerPoolPorts.ITROSPECTION, componentURI);
		
		return result;
	}

}
