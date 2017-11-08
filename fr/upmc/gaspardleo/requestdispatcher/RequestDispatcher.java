package fr.upmc.gaspardleo.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatcher 
extends AbstractComponent 
implements RequestDispatcherI, RequestSubmissionHandlerI , RequestNotificationHandlerI {

	public static enum	RDPortTypes {
		REQUEST_SUBMISSION_IN, REQUEST_NOTIFICATION_OUT
	}
	
	private String dispatcherUri;

	private ArrayList<String> registeredVmsUri;
	private ArrayList<RequestSubmissionOutboundPort> registeredVmsRsop;

	private RequestSubmissionOutboundPort rsop;
	private RequestSubmissionInboundPort rsip;

	private RequestNotificationInboundPort rnip;
	private RequestNotificationOutboundPort rnop;

	private Integer vmCursor;

	public RequestDispatcher(String dispatcherUri) throws Exception {
		super(1, 1);

		this.dispatcherUri = dispatcherUri;
		this.registeredVmsUri = new ArrayList<>();
		this.registeredVmsRsop = new ArrayList<>();

		this.vmCursor = 0;

		// Request submission inbound port connection.
		this.rsip = new RequestSubmissionInboundPort(this);
		this.addPort(this.rsip);
		this.rsip.publishPort();

		// Request submission outbound port connection.
		this.rsop = new RequestSubmissionOutboundPort(this) ;
		this.addPort(this.rsop);
		this.rsop.publishPort();
		this.addOfferedInterface(RequestSubmissionHandlerI.class) ;

		// Request notification submission inbound port connection.
		this.rnip = new RequestNotificationInboundPort(this);
		this.addPort(this.rnip);
		this.rnip.publishPort();

		// Request notification submission outbound port connection.
		this.rnop = new RequestNotificationOutboundPort(this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		this.addOfferedInterface(RequestNotificationI.class);

	}

	public void registerVM(String vmUri, String requestSubmissionInboundPort) throws Exception {
	
		if (this.registeredVmsUri.contains(vmUri)) 
			return;

		RequestSubmissionOutboundPort rsop = new RequestSubmissionOutboundPort(this);
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
		
		this.logMessage(this.dispatcherUri + " : incoming request submission");
		
		if (registeredVmsRsop.size() == 0) {
			this.logMessage(this.dispatcherUri + " : no registered vm.");
		} else {
			RequestSubmissionOutboundPort rsop = registeredVmsRsop.get(vmCursor%registeredVmsRsop.size()); 

			if (!rsop.connected()) {
				throw new Exception(this.dispatcherUri + " can't conect to vm.");
			}

			rsop.submitRequest(r);

			vmCursor++;
		}

	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		this.logMessage(this.dispatcherUri + " : incoming request submission and notification.");

		if (registeredVmsRsop.size() == 0) {
			this.logMessage(this.dispatcherUri + " : no registered vm.");
		} else {
			vmCursor = (vmCursor+1)%registeredVmsRsop.size();
			RequestSubmissionOutboundPort rsop = registeredVmsRsop.get(vmCursor); 
			this.logMessage(this.dispatcherUri + " is using " + registeredVmsUri.get(vmCursor));

			if (!rsop.connected()) {
				throw new Exception(this.dispatcherUri + " can't conect to vm.");
			}

			rsop.submitRequestAndNotify(r);
		}
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		this.logMessage(this.dispatcherUri + " : incoming request termination notification.");
		// TODO traitement de la notification de terminaison d'une requête
		// TODO Mimer un retour client
	}
	
	public Map<RDPortTypes, String>	getRDPortsURI() throws Exception {
		HashMap<RDPortTypes, String> ret =
				new HashMap<RDPortTypes, String>() ;		
		ret.put(RDPortTypes.REQUEST_SUBMISSION_IN,
				this.rsip.getPortURI()) ;
		ret.put(RDPortTypes.REQUEST_NOTIFICATION_OUT,
				this.rnop.getPortURI()) ;
		return ret ;
	}

	@Override
	public void connectionWithRG(String rgUri) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
