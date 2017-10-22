package fr.upmc.gaspardleo.step1.step11.requestdispatcher;

import java.util.ArrayList;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.gaspardleo.step1.step11.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatcher 
extends AbstractComponent 
implements RequestDispatcherI, RequestSubmissionHandlerI , RequestNotificationHandlerI {

	private String dispatcherUri;

	private ArrayList<String> registeredVmsUri;
	private ArrayList<RequestSubmissionOutboundPort> registeredVmsRsop;

	private RequestSubmissionOutboundPort rsop;
	private RequestSubmissionInboundPort rsip;

	private RequestNotificationInboundPort rnip;
	private RequestNotificationOutboundPort rnop;

	private Integer vmCursor;

	public RequestDispatcher(String dispatcherUri, 
			String requestSubmissionInboundPortUri, String reqSubmissionOutboundPortUri,
			String requestNotificationInboundPortUri, String requestNotificationOutboundPortUri) throws Exception {
		super(1, 1);

		assert requestSubmissionInboundPortUri != null;
		assert requestNotificationInboundPortUri != null;
		assert requestNotificationOutboundPortUri != null;

		this.dispatcherUri = dispatcherUri;
		this.registeredVmsUri = new ArrayList<>();
		this.registeredVmsRsop = new ArrayList<>();

		this.vmCursor = 0;


		// Request submission inbound port connection.
		this.rsip = new RequestSubmissionInboundPort(requestSubmissionInboundPortUri, this);
		this.addPort(this.rsip);
		this.rsip.publishPort();

		// Request submission outbound port connection.
		this.rsop = new RequestSubmissionOutboundPort(reqSubmissionOutboundPortUri, this) ;
		this.addPort(this.rsop);
		this.rsop.publishPort();
		this.addOfferedInterface(RequestSubmissionHandlerI.class) ;

		// Request notification submission inbound port connection.
		this.rnip = new RequestNotificationInboundPort(requestNotificationInboundPortUri, this);
		this.addPort(this.rnip);
		this.rnip.publishPort();

		// Request notification submission outbound port connection.
		this.rnop = new RequestNotificationOutboundPort(requestNotificationOutboundPortUri, this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		this.addOfferedInterface(RequestNotificationI.class);

	}

	public void registerVM(String vmUri, String requestSubmissionInboundPort) throws Exception {
	
		String portUri = AbstractPort.generatePortURI();

		if (this.registeredVmsUri.contains(vmUri)) 
			return;

		RequestSubmissionOutboundPort rsop = new RequestSubmissionOutboundPort(portUri, this);
		this.addPort(rsop);
		rsop.publishPort();

		rsop.doConnection(requestSubmissionInboundPort, RequestSubmissionConnector.class.getCanonicalName());

		registeredVmsRsop.add(rsop);
		registeredVmsUri.add(registeredVmsUri.size(), vmUri);
		
		this.logMessage(this.dispatcherUri + " : " + vmUri + " has been added.");
	}


	public void unregisterVM(String vmUri) throws Exception {
		// TODO
		registeredVmsRsop.remove(vmUri);
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {		
		this.logMessage(dispatcherUri + " : incoming request submission");
		
		if (registeredVmsRsop.size() == 0) {
			this.logMessage(dispatcherUri + " : no registered vm.");
		} else {
			RequestSubmissionOutboundPort rsop = registeredVmsRsop.get(vmCursor%registeredVmsRsop.size()); 

			if (!rsop.connected()) {
				throw new Exception(dispatcherUri + " can't conect to vm.");
			}

			rsop.submitRequest(r);

			vmCursor++;
		}

	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		this.logMessage(dispatcherUri + " : incoming request submission and notification.");

		if (registeredVmsRsop.size() == 0) {
			this.logMessage(dispatcherUri + " : no registered vm.");
		} else {
			vmCursor = (vmCursor+1)%registeredVmsRsop.size();
			RequestSubmissionOutboundPort rsop = registeredVmsRsop.get(vmCursor); 
			this.logMessage(dispatcherUri + " is using " + registeredVmsUri.get(vmCursor));

			if (!rsop.connected()) {
				throw new Exception(dispatcherUri + " can't conect to vm.");
			}

			rsop.submitRequestAndNotify(r);


		}
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		this.logMessage(dispatcherUri + " : incoming request termination notification.");
		// TODO
	}	
}
