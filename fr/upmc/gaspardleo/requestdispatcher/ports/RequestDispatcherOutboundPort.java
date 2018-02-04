package fr.upmc.gaspardleo.requestdispatcher.ports;

import java.util.HashMap;
import java.util.List;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

/**
 * La classe <code> RequestDispatcherOutboundPort </ code> implémente le port sortrant 
 * offrant l'interface <code> RequestDispatcherI </ code>.
 * @author Leonor & Alexandre
 */
public class RequestDispatcherOutboundPort 
		extends AbstractOutboundPort
		implements RequestDispatcherI{

	private static final long serialVersionUID = 1L;

	/**
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public RequestDispatcherOutboundPort(ComponentI owner) throws Exception {
		super(RequestDispatcherI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI#registerVM(final HashMap<ApplicationVMPortTypes, String>, Class<?>)
	 */
	@Override
	public String registerVM(HashMap<ApplicationVMPortTypes, String> vmUri, Class<?> vmInterface)
			throws Exception {
		return ((RequestDispatcherI)this.connector).registerVM(vmUri, vmInterface);
	}

	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI#unregisterVM(final String)
	 */
	@Override
	public void unregisterVM(String vmUri) throws Exception {
		((RequestDispatcherI)this.connector).unregisterVM(vmUri);
	}
	
	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI#unregisterVM()
	 */
	@Override
	public void unregisterVM() throws Exception {
		((RequestDispatcherI)this.connector).unregisterVM();
	}

	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI#getRegisteredAVMUris()
	 */
	@Override
	public List<String> getRegisteredAVMUris() throws Exception {
		return ((RequestDispatcherI)this.connector).getRegisteredAVMUris();
	}
}
