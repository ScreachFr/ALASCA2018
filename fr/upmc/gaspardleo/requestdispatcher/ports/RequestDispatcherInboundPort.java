package fr.upmc.gaspardleo.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatcherInboundPort 
	extends AbstractInboundPort 
	implements RequestDispatcherI{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	public RequestDispatcherInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RequestDispatcherI.class, owner);
	}

	@Override
	public void registerVM(final String vmUri, final String requestSubmissionOutboundPort) throws Exception {
		final RequestDispatcher requestDispatcher = (RequestDispatcher)this.owner;
		requestDispatcher.handleRequestAsync(
				new ComponentI.ComponentService<RequestDispatcher>(){
					@Override
					public RequestDispatcher call() throws Exception {
						requestDispatcher.registerVM(vmUri, requestSubmissionOutboundPort);
						return requestDispatcher;
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
	public void connectionWithRG(String rgUri) throws Exception {
		final RequestDispatcher requestDispatcher = (RequestDispatcher)this.owner;
		requestDispatcher.handleRequestAsync(
				new ComponentI.ComponentService<RequestDispatcher>(){
					@Override
					public RequestDispatcher call() throws Exception {
						requestDispatcher.connectionWithRG(rgUri);
						return requestDispatcher;
					}});
	}

}
