package fr.upmc.gaspardleo.componentmanagement.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;

public class ShutdownableOutboundPort 
		extends AbstractOutboundPort
		implements ShutdownableI{

	private static final long serialVersionUID = 1L;

	public ShutdownableOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ShutdownableI.class, owner);
	}

	@Override
	public void shutdown() throws Exception {
		((ShutdownableI)(this.connector)).shutdown();
	}

}
