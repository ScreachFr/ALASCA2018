package fr.upmc.gaspardleo.requestdispatcher.ports;

import java.util.List;
import java.util.Map;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatcherInboundPort 
	extends AbstractInboundPort 
	implements RequestDispatcherI{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	public RequestDispatcherInboundPort(ComponentI owner) throws Exception {
		super(RequestDispatcherI.class, owner);
	}
	public RequestDispatcherInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RequestDispatcherI.class, owner);
	}

	@Override
	public String registerVM(final Map<ApplicationVMPortTypes, String> vmUri, Class<?> vmInterface) throws Exception {
		final RequestDispatcher requestDispatcher = (RequestDispatcher)this.owner;
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<String>(){
					@Override
					public String call() throws Exception {
						return requestDispatcher.registerVM(vmUri, vmInterface);
					}});
	}

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
