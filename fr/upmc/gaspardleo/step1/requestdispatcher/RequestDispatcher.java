package fr.upmc.gaspardleo.step1.requestdispatcher;

import java.util.HashSet;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.gaspardleo.step1.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatcher 
	extends AbstractComponent 
	implements RequestDispatcherI, RequestSubmissionHandlerI , RequestNotificationHandlerI {
	
	private String dispatcherUri;
	private Set<String> registeredVms;
	
	private RequestSubmissionOutboundPort reqSubmissionOutboundPort;
	private RequestSubmissionInboundPort reqSubmissionInboundPort;
	
	private RequestNotificationInboundPort rnip;
	private RequestNotificationOutboundPort rnop;
	
	
	public RequestDispatcher(String dispatcherUri, 
			String requestSubmissionInboundPortUri, String requestSubmissionOutboundPortUri,
			String requestNotificationInboundPortUri, String requestNotificationOutboundPortUri) throws Exception {
		super(1, 1);
		
		assert requestSubmissionInboundPortUri != null;
		assert requestSubmissionOutboundPortUri != null;
		assert requestNotificationInboundPortUri != null;
		assert requestNotificationOutboundPortUri != null;
		
		this.dispatcherUri = dispatcherUri;
		this.registeredVms = new HashSet<>();
		
		
		
		// Request submission inbound port connection.
		this.reqSubmissionInboundPort = new RequestSubmissionInboundPort(requestSubmissionInboundPortUri, this);
		this.addPort(this.reqSubmissionInboundPort);
		this.reqSubmissionInboundPort.publishPort();
		this.addOfferedInterface(RequestSubmissionHandlerI.class) ;
		
		// Request submission outbound port connection.
		this.reqSubmissionOutboundPort = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortUri, this) ;
		this.addPort(this.reqSubmissionOutboundPort);
		this.reqSubmissionOutboundPort.publishPort();
		this.addOfferedInterface(RequestSubmissionHandlerI.class) ;
		
		// Request notification submission inbound port connection.
		this.rnip = new RequestNotificationInboundPort(requestNotificationInboundPortUri, this);
		this.addPort(this.rnip);
		this.rnip.publishPort();
		this.addOfferedInterface(RequestNotificationI.class);
		
		// Request notification submission outbound port connection.
		this.rnop = new RequestNotificationOutboundPort(requestNotificationOutboundPortUri, this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		this.addOfferedInterface(RequestNotificationI.class);
		
	}
	
	public void registerVM(String vmUri) throws Exception {
		registeredVms.add(vmUri);
	}
	
	public void unregisterVM(String vmUri) throws Exception {
		registeredVms.remove(vmUri);
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		this.logMessage(dispatcherUri + " : accepted a request submission.");
		// TODO
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		this.logMessage(dispatcherUri + " : accepted a request submission and notification.");
		// TODO
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		this.logMessage(dispatcherUri + " : accepted a request termination notification.");
		// TODO
	}	
}
