package fr.upmc.gaspardleo.computerpool.ports;

import java.util.Map;
import java.util.Set;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

public class ComputerPooOutboundPort extends AbstractOutboundPort implements ComputerPoolI {

	public ComputerPooOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ComputerPoolI.class, owner);
	}

	@Override
	public void createNewComputer(String computerURI, Set<Integer> possibleFrequencies,
			Map<Integer, Integer> processingPower, int defaultFrequency, int maxFrequencyGap, int numberOfProcessors,
			int numberOfCores) throws Exception {
		((ComputerPoolI)(this.connector)).createNewComputer(computerURI, possibleFrequencies, processingPower,
				defaultFrequency, maxFrequencyGap, numberOfProcessors, numberOfCores);
		
	}

	@Override
	public Map<ApplicationVMPortTypes, String> createNewApplicationVM(String avmURI, int numberOfCoreToAllocate) throws Exception {
		return ((ComputerPoolI)(this.connector)).createNewApplicationVM(avmURI, numberOfCoreToAllocate);
	}

}
