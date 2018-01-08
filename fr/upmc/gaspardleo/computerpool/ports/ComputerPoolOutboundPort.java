package fr.upmc.gaspardleo.computerpool.ports;

import java.util.HashMap;
import java.util.HashSet;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

public class ComputerPoolOutboundPort extends AbstractOutboundPort implements ComputerPoolI {

	public ComputerPoolOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ComputerPoolI.class, owner);
	}

	@Override
	public void createNewComputer(String computerURI,
			HashSet<Integer> possibleFrequencies,
			HashMap<Integer, Integer> processingPower,
			Integer defaultFrequency,
			Integer maxFrequencyGap,
			Integer numberOfProcessors,
			Integer numberOfCores) throws Exception {
		((ComputerPoolI)(this.connector)).createNewComputer(computerURI, possibleFrequencies, processingPower,
				defaultFrequency, maxFrequencyGap, numberOfProcessors, numberOfCores);
		
	}

	@Override
	public HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(String avmURI, Integer numberOfCoreToAllocate) throws Exception {
		return ((ComputerPoolI)(this.connector)).createNewApplicationVM(avmURI, numberOfCoreToAllocate);
	}

	@Override
	public Boolean hasAvailableCore() throws Exception {
		return ((ComputerPoolI)(this.connector)).hasAvailableCore();
	}

	@Override
	public Boolean increaseCoreFrequency(String avmUri) throws Exception {
		return ((ComputerPoolI)(this.connector)).increaseCoreFrequency(avmUri);
	}

	@Override
	public Boolean decreaseCoreFrequency(String avmUri) throws Exception {
		return ((ComputerPoolI)(this.connector)).decreaseCoreFrequency(avmUri);
	}

	@Override
	public void releaseCores(String avmUri) throws Exception {
		((ComputerPoolI)(this.connector)).releaseCores(avmUri);
	}

}
