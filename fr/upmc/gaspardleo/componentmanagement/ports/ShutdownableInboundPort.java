package fr.upmc.gaspardleo.componentmanagement.ports;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;

public class ShutdownableInboundPort extends AbstractInboundPort implements ShutdownableI {
	private static final long serialVersionUID = 3417493372016237408L;

	public ShutdownableInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ShutdownableI.class, owner);
	}

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
