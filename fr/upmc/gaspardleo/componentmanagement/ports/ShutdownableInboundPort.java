package fr.upmc.gaspardleo.componentmanagement.ports;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;

/**
 * La classe <code> ShutdownableInboundPort </ code> implémente le port entrant 
 * offrant l'interface <code> ShutdownableI </ code>.
 * @author Leonor & Alexandre
 */
public class ShutdownableInboundPort 
		extends AbstractInboundPort 
		implements ShutdownableI {

	private static final long serialVersionUID = 3417493372016237408L;

	/**
	 * @param uri			URI du port
	 * @param owner			Composant propriétaire du port
	 * @throws Exception
	 */
	public ShutdownableInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ShutdownableI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.componentmanagement#shutdown()
	 */
	@Override
	public void shutdown() throws Exception {
		ShutdownableI component = (ShutdownableI) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<AbstractComponent>(){
					@Override
					public RequestDispatcher call() throws Exception {
						component.shutdown();
						return null;
					}});
	}

}
