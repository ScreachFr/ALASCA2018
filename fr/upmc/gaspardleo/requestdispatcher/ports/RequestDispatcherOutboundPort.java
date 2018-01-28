package fr.upmc.gaspardleo.requestdispatcher.ports;

import java.util.HashMap;
import java.util.List;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatcherOutboundPort 
		extends AbstractOutboundPort
		implements RequestDispatcherI{

	private static final long serialVersionUID = 1L;

	public RequestDispatcherOutboundPort(ComponentI owner) throws Exception {
		super(RequestDispatcherI.class, owner);
	}

	@Override
	public String registerVM(HashMap<ApplicationVMPortTypes, String> vmUri, Class<?> vmInterface)
			throws Exception {
		return ((RequestDispatcherI)this.connector).registerVM(vmUri, vmInterface);
	}

	@Override
	public void unregisterVM(String vmUri) throws Exception {
		((RequestDispatcherI)this.connector).unregisterVM(vmUri);
	}
	
	@Override
	public void unregisterVM() throws Exception {
		((RequestDispatcherI)this.connector).unregisterVM();
	}

	@Override
	public List<String> getRegisteredAVMUris() throws Exception {
		return ((RequestDispatcherI)this.connector).getRegisteredAVMUris();
	}
}
