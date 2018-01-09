package fr.upmc.gaspardleo.computerpool.connectors;

import java.util.HashMap;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

public class ComputerPoolConnector 
		extends AbstractConnector
		implements ComputerPoolI{

	@Override
	public HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(String avmURI, Integer numberOfCoreToAllocate,
			ComponentCreator cc) throws Exception {
		return ((ComputerPoolI)this.offering).createNewApplicationVM(avmURI, numberOfCoreToAllocate, cc);
	}

	@Override
	public void addComputer(HashMap<ComputerPortsTypes, String> computerUris, Integer numberOfProcessors,
			Integer numberOfCores) throws Exception {
		((ComputerPoolI)this.offering).addComputer(computerUris, numberOfProcessors, numberOfCores);
	}
}
