package fr.upmc.gaspardleo.computerpool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.computer.Computer;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.exceptions.NoAvailableResourceException;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolInbounPort;

public class ComputerPool 
		extends AbstractComponent 
		implements ComputerPoolI {
	
	public enum ComputerPoolPorts {
		COMPUTER_POOL;
	}
	
	public final static int DEFAULT_CORE_ALLOC_NUMBER = 2;
	private ComputerPoolInbounPort cpi;
	private List<Map<ComputerPortsTypes, String>> computers;
	private List<AllocatedCore[]> availableCores;
	private Map<Map<ApplicationVMPortTypes, String>, AllocatedCore[]> avmInUse;
	
	public ComputerPool(
			String computerPoolPort_URI) throws Exception {
				
		super(1, 1);
				
		this.addOfferedInterface(ComputerPoolI.class);
		this.cpi = new ComputerPoolInbounPort(computerPoolPort_URI, this);
		this.cpi.publishPort();
		this.addPort(this.cpi);
		
		System.out.println("[DEBUG LEO] yoooooo");
		
		assert cpi.isPublished();
		
		this.computers = new ArrayList<>();
		this.availableCores = new ArrayList<>();
		this.avmInUse = new HashMap<>();
		
		this.toggleLogging();
	}

	@Override
	public void createNewComputer(
			String computerURI,
			HashSet<Integer> possibleFrequencies,
			HashMap<Integer, Integer> processingPower,
			Integer defaultFrequency,
			Integer maxFrequencyGap,
			Integer numberOfProcessors,
			Integer numberOfCores,
			ComponentCreator cc) throws Exception {

		System.out.println("Computer creation and core allocation.");
		
		Map<ComputerPortsTypes, String> computerUris = Computer.newInstance(computerURI, possibleFrequencies, 
				processingPower, defaultFrequency, maxFrequencyGap, numberOfProcessors, numberOfCores, cc);

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
	public Map<ApplicationVMPortTypes, String> createNewApplicationVM(
			String avmURI, 
			Integer numberOfCoreToAllocate, 
			ComponentCreator cc)  throws Exception{
		
		// Pas de core sous la main.
		if (availableCores.size() == 0)
			throw new NoAvailableResourceException();

		Map<ApplicationVMPortTypes, String> result = ApplicationVM.newInstance(avmURI, cc);
		
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
	
	public static Map<ComputerPoolPorts, String> newInstance(
			ComponentCreator cc) throws Exception {
		
		String computerPoolPort_URI = AbstractPort.generatePortURI();
				
		Object[] constructorParams = new Object[]{
				computerPoolPort_URI
		};
		
		try{
			cc.createComponent(ComputerPool.class, constructorParams);
		}catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		
		HashMap<ComputerPoolPorts, String> result = new HashMap<>();
		result.put(ComputerPoolPorts.COMPUTER_POOL, computerPoolPort_URI);
		
		return result;
	}
}
