package fr.upmc.gaspardleo.applicationvm.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;

public class ApplicationVMConnectionInboundPort 
	extends AbstractInboundPort implements ApplicationVMConnectionsI{

	private static final long serialVersionUID = 6418760843805692699L;

	public ApplicationVMConnectionInboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, ApplicationVMConnectionsI.class, owner);
	}

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

}
