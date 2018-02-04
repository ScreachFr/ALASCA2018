package fr.upmc.gaspardleo.computerpool;

import java.util.HashMap;

import fr.upmc.components.AbstractComponent;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolNetworkMasterInboundPort;

/**
 * La classe <code>ComputerPoolNetworkMaster</ code> implémente le composant représentant 
 * un registre de <code>ComputerPool</code>.
 * 
 * <p><strong>Description</strong></p>
 * Ce composant permet de disposer d'une liste de <code>ComputerPool</code> afin que le <code>PerformanceRegulator</code>
 * puisse choisir celui qui lui convient.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		ComputerPoolNetworkMaster 
		extends 	AbstractComponent
		implements 	ComputerPoolNetworkMasterI{
	
	/** URI du composant */
	private String uri;
	
	/** Registe de ComputerPool. <URI du ComputerPool, URI du port ComputerPoolI in> */
	private HashMap<String, String> pools;
	
	/** Port serveur du ComputerPoolNetwordMasterI */
	private ComputerPoolNetworkMasterInboundPort cpnmip;
	
	/**
	 * @param 	componentUri 				URI du composant.
	 * @param 	computerPoolInboundPortUri 	URI du port ComputerPoolNetworkMaster in.
	 * @throws 	Exception
	 */
	public ComputerPoolNetworkMaster(String componentUri, String computerPoolInboundPortUri) throws Exception {
		super(1, 1);
		
		this.uri = componentUri;
		
		this.pools = new HashMap<>();
		
		this.addOfferedInterface(ComputerPoolNetworkMasterI.class);
		this.cpnmip = new ComputerPoolNetworkMasterInboundPort(computerPoolInboundPortUri, this);
		this.cpnmip.publishPort();
		this.addPort(cpnmip);
		
		// Debug
		this.toggleLogging();
		this.toggleTracing();
		
		this.logMessage(uri + " has been created!");
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI#registerComputerPool(String, String)
	 */
	@Override
	public void registerComputerPool(String computerPoolUri, String compterPoolInboundPortUri) {
		pools.put(computerPoolUri, compterPoolInboundPortUri);
		
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI#unregisterComputerPool(String)
	 */
	@Override
	public void unregisterComputerPool(String computerPoolUri) {
		pools.remove(computerPoolUri);
	}

	/**
	 * @see fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI#getAvailableComputerPools()
	 */
	@Override
	public HashMap<String, String> getAvailableComputerPools() {
		return pools;
	}
}
