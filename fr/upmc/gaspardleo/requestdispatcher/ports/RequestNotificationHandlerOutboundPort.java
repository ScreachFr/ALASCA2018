package fr.upmc.gaspardleo.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;

public class RequestNotificationHandlerOutboundPort 
			extends AbstractOutboundPort 
			implements RequestNotificationHandlerI {

	public RequestNotificationHandlerOutboundPort(ComponentI owner) throws Exception {
		super(RequestNotificationHandlerI.class, owner);
	}

	public RequestNotificationHandlerOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RequestNotificationHandlerI.class, owner) ;

		assert	uri != null ;
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		System.out.println("ok");
		((RequestNotificationHandlerI)this.connector).acceptRequestTerminationNotification(r);
	}
}
