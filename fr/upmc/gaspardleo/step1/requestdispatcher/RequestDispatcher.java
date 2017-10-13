package fr.upmc.gaspardleo.step1.requestdispatcher;

import java.util.HashSet;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.interfaces.RequestGeneratorManagementI;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementInboundPort;
import fr.upmc.gaspardleo.step1.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatcher 
	extends AbstractComponent 
	implements RequestDispatcherI, RequestSubmissionHandlerI{
	
	private String dispatcherUri;
	private Set<String> registeredVms;
	
	private RequestSubmissionOutboundPort reqSubmissionOutboundPort;
	private RequestSubmissionInboundPort reqSubmissionInboundPort;
	
	public RequestDispatcher(String dispatcherUri, 
			String requestSubmissionInboundPortUri, String requestSubmissionOutboundPortUri) throws Exception {
		super(1, 1);
		
		this.dispatcherUri = dispatcherUri;
		this.registeredVms = new HashSet<>();
		
		// Request submission outbound port connection.
		this.addOfferedInterface(RequestSubmissionHandlerI.class) ;
		this.reqSubmissionOutboundPort = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortUri, this) ;
		this.addPort(this.reqSubmissionOutboundPort);
		this.reqSubmissionOutboundPort.publishPort();
		
		// Request submission inbound port connection.
		this.addOfferedInterface(RequestSubmissionHandlerI.class) ;
		this.reqSubmissionInboundPort = new RequestSubmissionInboundPort(requestSubmissionInboundPortUri, this);
		this.addPort(this.reqSubmissionInboundPort);
		this.reqSubmissionInboundPort.publishPort();
	}
	
	public void registerVM(String vmUri) throws Exception {
		registeredVms.add(vmUri);
	}
	
	public void unregisterVM(String vmUri) throws Exception {
		registeredVms.remove(vmUri);
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		// TODO Auto-generated method stub
		
	}	
}
