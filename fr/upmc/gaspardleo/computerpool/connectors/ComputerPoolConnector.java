package fr.upmc.gaspardleo.computerpool.connectors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

public class ComputerPoolConnector 
	extends AbstractConnector
	implements ComputerPoolI{

	@Override
	public void createNewComputer(String computerURI, HashSet<Integer> possibleFrequencies,
			HashMap<Integer, Integer> processingPower, Integer defaultFrequency, Integer maxFrequencyGap,
			Integer numberOfProcessors, Integer numberOfCores) throws Exception {
		((ComputerPoolI)this.offering).createNewComputer(computerURI, possibleFrequencies, processingPower, defaultFrequency, maxFrequencyGap, numberOfProcessors, numberOfCores);
	}

	@Override
	public Map<ApplicationVMPortTypes, String> createNewApplicationVM(String avmURI, Integer numberOfCoreToAllocate)
			throws Exception {
		return ((ComputerPoolI)this.offering).createNewApplicationVM(avmURI, numberOfCoreToAllocate);
	}

}
