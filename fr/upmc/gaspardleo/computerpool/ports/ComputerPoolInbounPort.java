package fr.upmc.gaspardleo.computerpool.ports;

import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

/**
 * La classe <code> ComputerPoolInbounPort </ code> implémente le port entrant 
 * offrant l'interface <code> ComputerPoolI </ code>.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		ComputerPoolInbounPort 
		extends 	AbstractOutboundPort
		implements 	ComputerPoolI {

	private static final long serialVersionUID = 1L;

	/**
	 * @param 	uri			URI du port.
	 * @param 	owner		Composant propriétaire du port.
	 * @throws 	Exception
	 */
	public ComputerPoolInbounPort(String uri, ComponentI owner) throws Exception {
		super(uri, ComputerPoolI.class, owner);
	}
	
	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#addComputer(HashMap<ComputerPortsTypes, String>, Integer, Integer)
	 */
	@Override
	public void addComputer(
			HashMap<ComputerPortsTypes, String> computerUris,
			Integer numberOfProcessors,
			Integer numberOfCores) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		computerPool.handleRequestAsync(
				new ComponentI.ComponentService<ComputerPool>(){
					@Override
					public ComputerPool call() throws Exception {
						computerPool.addComputer(
								computerUris,
								numberOfProcessors,
								numberOfCores);
						return computerPool;
					}});
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#createNewApplicationVM(String, Integer)
	 */
	@Override
	public HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(
			String avmURI, 
			Integer numberOfCoreToAllocate) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		return computerPool.handleRequestSync(
				new ComponentI.ComponentService<HashMap<ApplicationVMPortTypes, String>>(){
					@Override
					public HashMap<ApplicationVMPortTypes, String> call() throws Exception {
						return computerPool.createNewApplicationVM(
								avmURI, 
								numberOfCoreToAllocate);
					}});
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#hasAvailableCore()
	 */
	@Override
	public Boolean hasAvailableCore() throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		return computerPool.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return computerPool.hasAvailableCore();
					}});
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#increaseCoreFrequency(String)
	 */
	@Override
	public Boolean increaseCoreFrequency(String avmUri) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		return computerPool.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return computerPool.increaseCoreFrequency(avmUri);
					}});
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#decreaseCoreFrequency(String)
	 */
	@Override
	public Boolean decreaseCoreFrequency(String avmUri) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		return computerPool.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return computerPool.decreaseCoreFrequency(avmUri);
					}});
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI#eleaseCores(String)
	 */
	@Override
	public void releaseCores(String avmUri) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		computerPool.handleRequestAsync(
				new ComponentI.ComponentService<ComputerPool>(){
					@Override
					public ComputerPool call() throws Exception {
						computerPool.releaseCores(avmUri);
						return computerPool;
					}});
	}
}
