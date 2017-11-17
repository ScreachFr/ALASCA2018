package fr.upmc.gaspardleo.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;

public class RequestNotificationHandlerInboundPort extends AbstractInboundPort implements RequestNotificationHandlerI {
	private static final long serialVersionUID = -9221552749755323867L;

	public RequestNotificationHandlerInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RequestNotificationHandlerI.class, owner);
	}

	public RequestNotificationHandlerInboundPort(ComponentI owner) throws Exception {
		super(RequestNotificationHandlerI.class, owner);
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		final RequestNotificationHandlerI rnh = (RequestNotificationHandlerI)this.owner;
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<RequestNotificationHandlerI>(){
					@Override
					public RequestNotificationHandlerI call() throws Exception {
						rnh.acceptRequestTerminationNotification(r);
						return rnh;
					}});

	}

}
