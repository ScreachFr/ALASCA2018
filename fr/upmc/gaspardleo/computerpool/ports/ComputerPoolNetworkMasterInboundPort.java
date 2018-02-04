package fr.upmc.gaspardleo.computerpool.ports;

import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.computerpool.ComputerPoolNetworkMaster;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI;

/**
 * La classe <code>ComputerPoolNetworkMasterInboundPort</ code> implémente le port entrant 
 * offrant l'interface <code>ComputerPoolNetworkMasterI</ code>.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		ComputerPoolNetworkMasterInboundPort 
		extends 	AbstractInboundPort 
		implements 	ComputerPoolNetworkMasterI {

	private static final long serialVersionUID = 7303969117519739268L;

	/**
	 * @param 	uri			URI du port.
	 * @param 	owner		Composant propriétaire du port.
	 * @throws 	Exception
	 */
	public ComputerPoolNetworkMasterInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ComputerPoolNetworkMasterI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI#registerComputerPool(String, String)
	 */
	@Override
	public void registerComputerPool(String computerPoolUri, String compterPoolInboundPortUri) throws Exception {
		final ComputerPoolNetworkMaster cpnm = (ComputerPoolNetworkMaster)this.owner;
		cpnm.handleRequestAsync(
				new ComponentI.ComponentService<ComputerPoolNetworkMaster>(){
					@Override
					public ComputerPoolNetworkMaster call() throws Exception {
						cpnm.registerComputerPool(computerPoolUri, compterPoolInboundPortUri);
						return cpnm;
					}});
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI#unregisterComputerPool(String)
	 */
	@Override
	public void unregisterComputerPool(String computerPoolUri) throws Exception {
		final ComputerPoolNetworkMaster cpnm = (ComputerPoolNetworkMaster)this.owner;
		cpnm.handleRequestAsync(
				new ComponentI.ComponentService<ComputerPoolNetworkMaster>(){
					@Override
					public ComputerPoolNetworkMaster call() throws Exception {
						cpnm.unregisterComputerPool(computerPoolUri);
						return cpnm;
					}});
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI#getAvailableComputerPools()
	 */
	@Override
	public HashMap<String, String> getAvailableComputerPools() throws Exception {
		final ComputerPoolNetworkMaster cpnm = (ComputerPoolNetworkMaster)this.owner;
		return cpnm.handleRequestSync(
				new ComponentI.ComponentService<HashMap<String, String>>(){
					@Override
					public HashMap<String, String> call() throws Exception {
						return cpnm.getAvailableComputerPools();
					}});
	}

}
