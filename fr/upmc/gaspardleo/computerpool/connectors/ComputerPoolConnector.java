package fr.upmc.gaspardleo.computerpool.connectors;

import java.util.HashMap;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

public class ComputerPoolConnector 
		extends AbstractConnector
		implements ComputerPoolI{

	@Override
	public HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(
			String avmURI, 
			Integer numberOfCoreToAllocate) throws Exception {
		return ((ComputerPoolI)this.offering).createNewApplicationVM(
				avmURI, 
				numberOfCoreToAllocate);
	}

	@Override
	public void addComputer(HashMap<ComputerPortsTypes, String> computerUris, Integer numberOfProcessors,
			Integer numberOfCores) throws Exception {
		((ComputerPoolI)this.offering).addComputer(computerUris, numberOfProcessors, numberOfCores);
	}

	@Override
	public void releaseCores(String avmUri) throws Exception {
		((ComputerPoolI)this.offering).releaseCores(avmUri);		
	}

	@Override
	public Boolean hasAvailableCore() throws Exception {
		return ((ComputerPoolI)this.offering).hasAvailableCore();
	}

	@Override
	public Boolean increaseCoreFrequency(String avmUri) throws Exception {
		return ((ComputerPoolI)this.offering).increaseCoreFrequency(avmUri);
	}

	@Override
	public Boolean decreaseCoreFrequency(String avmUri) throws Exception {
		return ((ComputerPoolI)this.offering).decreaseCoreFrequency(avmUri);
	}
}
