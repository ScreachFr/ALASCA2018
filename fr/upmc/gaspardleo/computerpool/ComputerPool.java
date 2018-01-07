package fr.upmc.gaspardleo.computerpool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.ComputerStaticState;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor.ProcessorPortTypes;
import fr.upmc.datacenter.hardware.processors.ProcessorDynamicState;
import fr.upmc.datacenter.hardware.processors.ProcessorStaticState;
import fr.upmc.datacenter.hardware.processors.connectors.ProcessorManagementConnector;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorManagementOutboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorStaticStateDataOutboundPort;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computer.Computer;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolInbounPort;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
	private Map<String, ProcessorManagementOutboundPort> processorManagmentPorts;
	private Map<String, ProcessorDynamicStateDataOutboundPort> processorDynamicStatePort;
	private Map<String, ProcessorStaticStateDataOutboundPort> processorStaticStatePort;
	

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
		this.processorManagmentPorts = new HashMap<>();
		this.processorDynamicStatePort = new HashMap<>();
		this.processorStaticStatePort = new HashMap<>();
		
		this.toggleLogging();
		this.toggleTracing();
	}

	@Override
	public synchronized void createNewComputer(String computerURI,
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

		ComputerStaticStateDataOutboundPort cssdop = new ComputerStaticStateDataOutboundPort(this, computerURI);
		this.addPort(cssdop);
		cssdop.publishPort();
		
		cssdop.doConnection(computerUris.get(ComputerPortsTypes.STATIC_STATE_IN),
				DataConnector.class.getCanonicalName());
		
		ComputerStaticState css = (ComputerStaticState) cssdop.request();
		
		
		
		int nbProcessor = css.getNumberOfProcessors();
		int nbCorePerProcessor = css.getNumberOfProcessors();
		
		Map<String, Map<ProcessorPortTypes, String>> cpuPortMap = css.getProcessorPortMap();
		
		for (String cpuUri : css.getProcessorURIs().values()) {
			
			ProcessorDynamicStateDataOutboundPort pdsdop 
				= new ProcessorDynamicStateDataOutboundPort(this, cpuUri);
			this.addPort(pdsdop);
			pdsdop.publishPort();
			pdsdop.doConnection(cpuPortMap.get(cpuUri).get(ProcessorPortTypes.DYNAMIC_STATE),
					DataConnector.class.getCanonicalName());
			
			processorDynamicStatePort.put(cpuUri, pdsdop);
			
			ProcessorStaticStateDataOutboundPort pssdop 
				= new ProcessorStaticStateDataOutboundPort(this, cpuUri);
			this.addPort(pssdop);
			pssdop.publishPort();
			pssdop.doConnection(cpuPortMap.get(cpuUri).get(ProcessorPortTypes.STATIC_STATE),
					DataConnector.class.getCanonicalName());
			
			processorStaticStatePort.put(cpuUri, pssdop);
			
			if (processorManagmentPorts.get(cpuUri) == null) {
				ProcessorManagementOutboundPort pmob = new ProcessorManagementOutboundPort(this);
				this.addPort(pmob);
				pmob.publishPort();
				
				pmob.doConnection(cpuPortMap.get(cpuUri).get(ProcessorPortTypes.MANAGEMENT),
						ProcessorManagementConnector.class.getCanonicalName());
				
				this.processorManagmentPorts.put(cpuUri, pmob);
			}
		}
		
		
		for (int i = 0; i < (nbProcessor * nbCorePerProcessor)/2; i++) {
			AllocatedCore[] cores = csop.allocateCores(DEFAULT_CORE_ALLOC_NUMBER);
			
			
			
			for (AllocatedCore allocatedCore : cores) {
				allocatedCore.processorInboundPortURI.get(ProcessorPortTypes.MANAGEMENT);
			}
			
			availableCores.add(cores);
		}
		
		this.computers.add(computerUris);
	}

	@Override
	public synchronized HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(String avmURI, Integer numberOfCoreToAllocate)  throws Exception{
		// Pas de core sous la main.
		if (availableCores.size() == 0)
			return null;

		HashMap<ApplicationVMPortTypes, String> result = ApplicationVM.newInstance(dcc, avmURI);
		
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
	
	@Override
	public Boolean increaseCoreFrequency(String avmUri) throws Exception {
		Boolean hasChangedFrequency = false;
		
		
		for (Map<ApplicationVMPortTypes, String> avm : avmInUse.keySet()) {
			if (avm.get(ApplicationVMPortTypes.INTROSPECTION).equals(avmUri)) {
				for (AllocatedCore core : avmInUse.get(avm)) {
					ProcessorDynamicStateDataOutboundPort pdsdop = processorDynamicStatePort.get(core.processorURI);
					ProcessorStaticStateDataOutboundPort pssdop = processorStaticStatePort.get(core.processorURI);
					
					ProcessorStaticState pss = (ProcessorStaticState) pssdop.request();
					ProcessorDynamicState pds = (ProcessorDynamicState) pdsdop.request();
					
					Optional<Integer> newCoreFreq = getNextAdmissibleFrequency(pss, pds, core.coreNo);
					
					if (newCoreFreq.isPresent()) {
						
						processorManagmentPorts.get(core.processorURI).setCoreFrequency(core.coreNo, newCoreFreq.get());
						hasChangedFrequency = true;
					}
					
				}
			}
			break;
		}
		
		return hasChangedFrequency;
	}
	
	private Optional<Integer> getNextAdmissibleFrequency(ProcessorStaticState pss, ProcessorDynamicState pds, Integer coreNo) {
		int currentFreq = pds.getCurrentCoreFrequency(coreNo);
		
		Set<Integer> admissibleFreqs = pss.getAdmissibleFrequencies();
		
		return admissibleFreqs.stream()
							  .filter(e -> e > currentFreq)
							  .min((x, y) -> Integer.compare(x, y));
	}
	
	@Override
	public Boolean decreaseCoreFrequency(String avmUri) throws Exception {
		throw new NotImplementedException();
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

	@Override
	public synchronized Boolean hasAvailableCore() throws Exception {
		return !availableCores.isEmpty();
	}

}
