package fr.upmc.gaspardleo.computerpool.ports;

import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI;

/**
 * La classe <code>ComputerPoolNetworkMasterOutboundPort</ code> implémente le port sortrant 
 * offrant l'interface <code>ComputerPoolNetworkMasterI</ code>.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		ComputerPoolNetworkMasterOutboundPort 
		extends 	AbstractOutboundPort 
		implements 	ComputerPoolNetworkMasterI {


	private static final long serialVersionUID = -6041921663130597612L;

	/**
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public ComputerPoolNetworkMasterOutboundPort(ComponentI owner) throws Exception {
		super(ComputerPoolNetworkMasterI.class, owner);
	}
	
	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI#registerComputerPool(String, String)
	 */
	@Override
	public void registerComputerPool(String computerPoolUri, String compterPoolInboundPortUri) throws Exception {
		((ComputerPoolNetworkMasterI)this.connector).registerComputerPool(computerPoolUri, compterPoolInboundPortUri);
		
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI#unregisterComputerPool(String)
	 */
	@Override
	public void unregisterComputerPool(String computerPoolUri) throws Exception {
		((ComputerPoolNetworkMasterI)this.connector).unregisterComputerPool(computerPoolUri);
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI#getAvailableComputerPools()
	 */
	@Override
	public HashMap<String, String> getAvailableComputerPools() throws Exception {
		return ((ComputerPoolNetworkMasterI)this.connector).getAvailableComputerPools();
	}
}
