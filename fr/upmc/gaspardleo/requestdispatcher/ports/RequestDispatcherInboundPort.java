package fr.upmc.gaspardleo.requestdispatcher.ports;

import java.util.HashMap;
import java.util.List;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

/**
 * La classe <code> RequestDispatcherInboundPort </ code> implémente le port entrant 
 * offrant l'interface <code> RequestDispatcherI </ code>.
 * @author Leonor & Alexandre
 */
public 	class 		RequestDispatcherInboundPort 
		extends 	AbstractInboundPort 
		implements 	RequestDispatcherI{

	private static final long serialVersionUID = 1L;
	
	/**
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public RequestDispatcherInboundPort(ComponentI owner) throws Exception {
		super(RequestDispatcherI.class, owner);
	}
	
	/**
	 * @param 	uri			URI de l'inbound port
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public RequestDispatcherInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RequestDispatcherI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces#registerVM(final HashMap<ApplicationVMPortTypes, String>, Class<?>)
	 */
	@Override
	public String registerVM(final HashMap<ApplicationVMPortTypes, String> vmUri, Class<?> vmInterface) throws Exception {
		final RequestDispatcher requestDispatcher = (RequestDispatcher)this.owner;
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<String>(){
					@Override
					public String call() throws Exception {
						return requestDispatcher.registerVM(vmUri, vmInterface);
					}});
	}

	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces#unregisterVM(final String)
	 */
	@Override
	public void unregisterVM(final String vmUri) throws Exception {
		final RequestDispatcher requestDispatcher = (RequestDispatcher)this.owner;
		requestDispatcher.handleRequestAsync(
				new ComponentI.ComponentService<RequestDispatcher>(){
					@Override
					public RequestDispatcher call() throws Exception {
						requestDispatcher.unregisterVM(vmUri);
						return requestDispatcher;
					}});
	}
	
	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces#unregisterVM()
	 */
	@Override
	public void unregisterVM() throws Exception {
		final RequestDispatcher requestDispatcher = (RequestDispatcher)this.owner;
		requestDispatcher.handleRequestAsync(
				new ComponentI.ComponentService<RequestDispatcher>(){
					@Override
					public RequestDispatcher call() throws Exception {
						requestDispatcher.unregisterVM();
						return requestDispatcher;
					}});
	}
	
	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces#getRegisteredAVMUris()
	 */
	@Override
	public List<String> getRegisteredAVMUris() throws Exception {
		final RequestDispatcher requestDispatcher = (RequestDispatcher)this.owner;
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<List<String>>(){
					@Override
					public List<String> call() throws Exception {
						return requestDispatcher.getRegisteredAVMUris();
					}});
	}
}
