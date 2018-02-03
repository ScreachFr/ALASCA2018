package fr.upmc.gaspardleo.applicationvm.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;

/**
 * La classe <code> ApplicationVMConnectionOutboundPort </ code> implémente le port sortant 
 * offrant l'interface <code> ApplicationVMConnectionsI </ code>.
 * @author Leonor & Alexandre
 */
public class ApplicationVMConnectionOutboundPort 
	extends AbstractOutboundPort 
	implements ApplicationVMConnectionsI {

	private static final long serialVersionUID = 1L;

	/**
	 * @param	uri			URI du port
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public ApplicationVMConnectionOutboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, ApplicationVMConnectionsI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.applicationvm.interfaces#doRequestNotificationConnection(String)
	 */
	@Override
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception {
		((ApplicationVMConnectionsI)this.connector).doRequestNotificationConnection(RD_RequestNotificationInboundPortURI);
	}

	/**
	 * @see fr.upmc.gaspardleo.applicationvm.interfaces#doRequestMonitorConnection(String)
	 */
	@Override
	public void doRequestMonitorConnection(String requestMonitor_in) throws Exception {
		((ApplicationVMConnectionsI)this.connector).doRequestMonitorConnection(requestMonitor_in);
	}

}
