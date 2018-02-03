package fr.upmc.gaspardleo.applicationvm.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;

/**
 * La classe <code> ApplicationVMConnectionInboundPort </ code> implémente le port entrant 
 * offrant l'interface <code> ApplicationVMConnectionsI </ code>.
 * @author Leonor & Alexandre
 */
public class ApplicationVMConnectionInboundPort 
		extends AbstractInboundPort 
		implements ApplicationVMConnectionsI{

	private static final long serialVersionUID = 6418760843805692699L;

	/**
	 * @param uri			URI du port
	 * @param owner			Composant propriétaire du port
	 * @throws Exception
	 */
	public ApplicationVMConnectionInboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, ApplicationVMConnectionsI.class, owner);
	}
	
	/**
	 * @see fr.upmc.gaspardleo.applicationvm.interfaces#doRequestNotificationConnection(String)
	 */
	@Override
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception {
		final ApplicationVM avm = (ApplicationVM)this.owner;
		avm.handleRequestAsync(
				new ComponentI.ComponentService<ApplicationVM>(){
					@Override
					public ApplicationVM call() throws Exception {
						avm.doRequestNotificationConnection(RD_RequestNotificationInboundPortURI);

						return avm;
					}});

		
	}

	/**
	 * @see fr.upmc.gaspardleo.applicationvm.interfaces#doRequestMonitorConnection(String)
	 */
	@Override
	public void doRequestMonitorConnection(String requestMonitor_in) throws Exception {
		final ApplicationVM avm = (ApplicationVM)this.owner;
		avm.handleRequestAsync(
				new ComponentI.ComponentService<ApplicationVM>(){
					@Override
					public ApplicationVM call() throws Exception {
						avm.doRequestMonitorConnection(requestMonitor_in);

						return avm;
					}});
	}

}
