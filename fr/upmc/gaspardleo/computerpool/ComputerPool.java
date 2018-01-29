package fr.upmc.gaspardleo.computerpool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.ComputerStaticState;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateDataI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor.ProcessorPortTypes;
import fr.upmc.datacenter.hardware.processors.ProcessorDynamicState;
import fr.upmc.datacenter.hardware.processors.ProcessorStaticState;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorManagementI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStaticStateDataI;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorManagementOutboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorStaticStateDataOutboundPort;
import fr.upmc.datacenter.interfaces.ControlledDataRequiredI;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentcreator.ComponentCreator;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolInbounPort;

public class ComputerPool 
		extends AbstractComponent 
		implements ComputerPoolI{
	
	public enum ComputerPoolPorts {
		INTROSPECTION,
		COMPUTER_POOL
	}
	
	public final static int DEFAULT_CORE_ALLOC_NUMBER = 2;
	private ComputerPoolInbounPort cpi;
	private List<HashMap<ComputerPortsTypes, String>> computers;
	private List<AllocatedCore[]> availableCores;
	private HashMap<HashMap<ApplicationVMPortTypes, String>, AllocatedCore[]> avmInUse;
	private String uri;
	private HashMap<String, ProcessorManagementOutboundPort> processorManagmentPorts;
	private HashMap<String, ProcessorDynamicStateDataOutboundPort> processorDynamicStatePort;
	private HashMap<String, ProcessorStaticStateDataOutboundPort> processorStaticStatePort;
	private ComponentCreator cc;

	public ComputerPool(
			HashMap<ComputerPoolPorts, String> component_uris) throws Exception {
		
		super(1, 1);

		this.uri = component_uris.get(ComputerPoolPorts.INTROSPECTION);

		this.addOfferedInterface(ComputerPoolI.class);
		this.cpi = new ComputerPoolInbounPort(component_uris.get(ComputerPoolPorts.COMPUTER_POOL), this);
		this.cpi.publishPort();
		this.addPort(this.cpi);
		
		this.computers = new ArrayList<>();
		this.availableCores = new ArrayList<>();
		this.avmInUse = new HashMap<>();
		this.processorManagmentPorts = new HashMap<>();
		this.processorDynamicStatePort = new HashMap<>();
		this.processorStaticStatePort = new HashMap<>();

		this.cc = new ComponentCreator(AbstractCVM.theCVM);
		
		this.toggleLogging();
		this.toggleTracing();
		
		this.logMessage("ComputerPool made");
	}
	
	@Override
	public void addComputer(
			HashMap<ComputerPortsTypes, String> computerUris,
			Integer numberOfProcessors,
			Integer numberOfCores) throws Exception {
		
		this.logMessage("Computer Pool : Computer creation and core allocation.");
		
		if(!this.isRequiredInterface(ComputerServicesI.class))
			this.addRequiredInterface(ComputerServicesI.class);
		
		ComputerServicesOutboundPort csop = new ComputerServicesOutboundPort(this);
		this.addPort(csop);
		csop.publishPort();
		
		csop.doConnection(
			computerUris.get(ComputerPortsTypes.SERVICE_IN), 
			ClassFactory.newConnector(ComputerServicesI.class).getCanonicalName());

		for (int i = 0; i < (numberOfProcessors * numberOfCores)/2; i++) 
			availableCores.add(csop.allocateCores(DEFAULT_CORE_ALLOC_NUMBER));
		
		
		this.computers.add(computerUris);
		
		csop.doConnection(computerUris.get(
			ComputerPortsTypes.SERVICE_IN),
			ClassFactory.newConnector(ComputerServicesI.class).getCanonicalName());
		
		if(!this.isRequiredInterface(ComputerStaticStateDataI.class))
			this.addRequiredInterface(ComputerStaticStateDataI.class);
		
		ComputerStaticStateDataOutboundPort cssdop = new ComputerStaticStateDataOutboundPort(this, computerUris.get(ComputerPortsTypes.INTROSPECTION));
		this.addPort(cssdop);
		cssdop.publishPort();

		cssdop.doConnection(
			computerUris.get(ComputerPortsTypes.STATIC_STATE_IN),
			DataConnector.class.getCanonicalName());
		
		ComputerStaticState css = (ComputerStaticState) cssdop.request();

		int nbProcessor = css.getNumberOfProcessors();
		int nbCorePerProcessor = css.getNumberOfProcessors();

		Map<String, Map<ProcessorPortTypes, String>> cpuPortMap = css.getProcessorPortMap();

		for (String cpuUri : css.getProcessorURIs().values()) {

			if(!this.isRequiredInterface(ControlledDataRequiredI.ControlledPullI.class))
				this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);
			
			ProcessorDynamicStateDataOutboundPort pdsdop = new ProcessorDynamicStateDataOutboundPort(this, cpuUri);
			this.addPort(pdsdop);
			pdsdop.publishPort();
			
			pdsdop.doConnection(
				cpuPortMap.get(cpuUri).get(ProcessorPortTypes.DYNAMIC_STATE_2),
				DataConnector.class.getCanonicalName());
			
			processorDynamicStatePort.put(cpuUri, pdsdop);

			if(!this.isRequiredInterface(ProcessorStaticStateDataI.class))
				this.addRequiredInterface(ProcessorStaticStateDataI.class);
			
			ProcessorStaticStateDataOutboundPort pssdop = new ProcessorStaticStateDataOutboundPort(this, cpuUri);
			this.addPort(pssdop);
			pssdop.publishPort();
			
			pssdop.doConnection(
				cpuPortMap.get(cpuUri).get(ProcessorPortTypes.STATIC_STATE_2),
				DataConnector.class.getCanonicalName());

			processorStaticStatePort.put(cpuUri, pssdop);

			if (processorManagmentPorts.get(cpuUri) == null) {

				if(!this.isRequiredInterface(ProcessorManagementI.class))
					this.addRequiredInterface(ProcessorManagementI.class);
				
				ProcessorManagementOutboundPort pmob = new ProcessorManagementOutboundPort(this);
				this.addPort(pmob);
				pmob.publishPort();
				
				pmob.doConnection(
					cpuPortMap.get(cpuUri).get(ProcessorPortTypes.MANAGEMENT),
					ClassFactory.newConnector(ProcessorManagementI.class).getCanonicalName());
			
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
	public synchronized  HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(
			String avmURI, 
			Integer numberOfCoreToAllocate)  throws Exception{
		
		// Pas de core sous la main.
		if (availableCores.size() == 0)
			return null;

		try{
			HashMap<ApplicationVMPortTypes, String> result = ApplicationVM.newInstance(avmURI, cc);
			// Create a mock up port to manage the AVM component (allocate cores).					
			
			if(!this.isRequiredInterface(ApplicationVMManagementI.class))
				this.addRequiredInterface(ApplicationVMManagementI.class);

			ApplicationVMManagementOutboundPort avmPort = new ApplicationVMManagementOutboundPort(this);
			this.addPort(avmPort);
			avmPort.publishPort();
	
			avmPort.doConnection(
				result.get(ApplicationVMPortTypes.MANAGEMENT),
				ClassFactory.newConnector(ApplicationVMManagementI.class).getCanonicalName());
			
			AllocatedCore[] cores = availableCores.remove(0);
			avmPort.allocateCores(cores);
	
			avmInUse.put(result, cores);
		
			return result;
			
		}catch (Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public Boolean increaseCoreFrequency(String avmUri) throws Exception {
		
		Boolean hasChangedFrequency = false;

		for (HashMap<ApplicationVMPortTypes, String> avm : avmInUse.keySet()) {
			if (avm.get(ApplicationVMPortTypes.INTROSPECTION).equals(avmUri)) {
				for (AllocatedCore core : avmInUse.get(avm)) {
					
					ProcessorDynamicStateDataOutboundPort pdsdop = processorDynamicStatePort.get(core.processorURI);
					ProcessorStaticStateDataOutboundPort pssdop = processorStaticStatePort.get(core.processorURI);

					System.out.println("pdsdop connected : " + pdsdop.connected());
					System.out.println("pssdop connceted : " + pssdop.connected());
					
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

	private Optional<Integer> getNextAdmissibleFrequency(
			ProcessorStaticState pss, ProcessorDynamicState pds, Integer coreNo) {
		
		int currentFreq = pds.getCurrentCoreFrequency(coreNo);

		Set<Integer> admissibleFreqs = pss.getAdmissibleFrequencies();

		int maxFrequency = Arrays.stream(pds.getCurrentCoreFrequencies()).max().getAsInt();
		int minFrequency = Arrays.stream(pds.getCurrentCoreFrequencies()).min().getAsInt();
		int maxGap = pss.getMaxFrequencyGap();
		
		return admissibleFreqs.stream()
				.filter(e -> (e > currentFreq) && isFrequencyGapAdmissible(minFrequency, maxFrequency, e, maxGap))
				.min((x, y) -> Integer.compare(x, y));
	}

	private Optional<Integer> getPreviousAdmissibleFequency(
			ProcessorStaticState pss, ProcessorDynamicState pds, Integer coreNo) {
		
		int currentFreq = pds.getCurrentCoreFrequency(coreNo);

		Set<Integer> admissibleFreqs = pss.getAdmissibleFrequencies();
		
		int maxFrequency = Arrays.stream(pds.getCurrentCoreFrequencies()).max().getAsInt();
		int minFrequency = Arrays.stream(pds.getCurrentCoreFrequencies()).min().getAsInt();
		int maxGap = pss.getMaxFrequencyGap();
		
		return admissibleFreqs.stream()
				.filter(e -> e < currentFreq && isFrequencyGapAdmissible(minFrequency, maxFrequency, e, maxGap))
				.max((x, y) -> Integer.compare(x, y));
	}
	
	private boolean isFrequencyGapAdmissible(int min, int max, int candidate, int maxGap) {
		return Math.max(Math.abs(candidate - min), Math.abs(max - candidate)) <= maxGap;
	}

	@Override
	public Boolean decreaseCoreFrequency(String avmUri) throws Exception {
		
		Boolean hasChangedFrequency = false;

		for (HashMap<ApplicationVMPortTypes, String> avm : avmInUse.keySet()) {
			if (avm.get(ApplicationVMPortTypes.INTROSPECTION).equals(avmUri)) {
				for (AllocatedCore core : avmInUse.get(avm)) {
					
					ProcessorDynamicStateDataOutboundPort pdsdop = processorDynamicStatePort.get(core.processorURI);
					ProcessorStaticStateDataOutboundPort pssdop = processorStaticStatePort.get(core.processorURI);

					ProcessorStaticState pss = (ProcessorStaticState) pssdop.request();
					ProcessorDynamicState pds = (ProcessorDynamicState) pdsdop.request();

					Optional<Integer> newCoreFreq = getPreviousAdmissibleFequency(pss, pds, core.coreNo);

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


	public String getUri() {
		return uri;
	}

	@Override
	public synchronized Boolean hasAvailableCore() throws Exception {
		return !availableCores.isEmpty();
	}

	@Override
	public synchronized void releaseCores(String avmUri) throws Exception {
		for (HashMap<ApplicationVMPortTypes, String> uris : avmInUse.keySet()) {
			if (uris.get(ApplicationVMPortTypes.INTROSPECTION).equals(avmUri)) {
				availableCores.add(avmInUse.remove(uris));
			}
		}
	}
	
	public static HashMap<ComputerPoolPorts, String> makeUris(){
		HashMap<ComputerPoolPorts, String> computerPool_uris = new HashMap<>();
		computerPool_uris.put(ComputerPoolPorts.COMPUTER_POOL, AbstractPort.generatePortURI());
		computerPool_uris.put(ComputerPoolPorts.INTROSPECTION, AbstractPort.generatePortURI());
		return computerPool_uris;
	}
}
