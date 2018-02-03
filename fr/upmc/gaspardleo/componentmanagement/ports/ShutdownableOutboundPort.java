package fr.upmc.gaspardleo.componentmanagement.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;

/**
 * La classe <code> ShutdownableInboundPort </ code> implémente le port sortant 
 * offrant l'interface <code> ShutdownableI </ code>.
 * @author Leonor & Alexandre
 */
public class ShutdownableOutboundPort 
		extends AbstractOutboundPort
		implements ShutdownableI{

	private static final long serialVersionUID = 1L;

	/**
	 * @param uri			URI du port
	 * @param owner			Composant propriétaire du port
	 * @throws Exception
	 */
	public ShutdownableOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ShutdownableI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.componentmanagement#shutdown()
	 */
	@Override
	public void shutdown() throws Exception {
		((ShutdownableI)(this.connector)).shutdown();
	}

}
