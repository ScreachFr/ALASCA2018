package fr.upmc.gaspardleo.computerpool.ports;

import java.util.HashMap;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.components.ports.AbstractPort;

import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

public class ComputerPoolOutboundPort 
		extends AbstractOutboundPort 
		implements ComputerPoolI {

	private static final long serialVersionUID = 1L;

	public ComputerPoolOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ComputerPoolI.class, owner);
	}

	public ComputerPoolOutboundPort(ComponentI owner) throws Exception {
		super(AbstractPort.generatePortURI(), ComputerPoolI.class, owner);
	}
	
	@Override
	public void addComputer(
			HashMap<ComputerPortsTypes, String> computerUris,
			Integer numberOfProcessors,
			Integer numberOfCores) throws Exception {
		
		((ComputerPoolI)(this.connector)).addComputer(
				computerUris,
				numberOfProcessors,
				numberOfCores);
	}

	@Override
	public HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(
			String avmURI, 
			Integer numberOfCoreToAllocate) throws Exception {
		
		return ((ComputerPoolI)(this.connector)).createNewApplicationVM(
				avmURI, 
				numberOfCoreToAllocate);
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
