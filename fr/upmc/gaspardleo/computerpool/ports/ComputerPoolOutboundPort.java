package fr.upmc.gaspardleo.computerpool.ports;

import java.util.HashMap;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

/**
 * La classe <code> ComputerPoolOutboundPort </ code> implémente le port sortrant 
 * offrant l'interface <code> ComputerPoolI </ code>.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		ComputerPoolOutboundPort 
		extends 	AbstractOutboundPort 
		implements 	ComputerPoolI {

	private static final long serialVersionUID = 1L;

	/**
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public ComputerPoolOutboundPort(ComponentI owner) throws Exception {
		super(ComputerPoolI.class, owner);
	}
	
	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#addComputer(HashMap<ComputerPortsTypes, String>, Integer, Integer)
	 */
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

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#createNewApplicationVM(String, Integer)
	 */
	@Override
	public HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(
			String avmURI, 
			Integer numberOfCoreToAllocate) throws Exception {
		
		return ((ComputerPoolI)(this.connector)).createNewApplicationVM(
				avmURI, 
				numberOfCoreToAllocate);
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#hasAvailableCore()
	 */
	@Override
	public Boolean hasAvailableCore() throws Exception {
		return ((ComputerPoolI)(this.connector)).hasAvailableCore();
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#increaseCoreFrequency(String)
	 */
	@Override
	public Boolean increaseCoreFrequency(String avmUri) throws Exception {
		return ((ComputerPoolI)(this.connector)).increaseCoreFrequency(avmUri);
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#decreaseCoreFrequency(String)
	 */
	@Override
	public Boolean decreaseCoreFrequency(String avmUri) throws Exception {
		return ((ComputerPoolI)(this.connector)).decreaseCoreFrequency(avmUri);
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#releaseCores(String)
	 */
	@Override
	public void releaseCores(String avmUri) throws Exception {
		((ComputerPoolI)(this.connector)).releaseCores(avmUri);
	}
}
