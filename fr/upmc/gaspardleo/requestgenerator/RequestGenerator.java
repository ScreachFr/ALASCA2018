package fr.upmc.gaspardleo.requestgenerator;

import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;
import fr.upmc.gaspardleo.requestgenerator.ports.RequestGeneratorInboundPort;

public class RequestGenerator 
		extends fr.upmc.datacenterclient.requestgenerator.RequestGenerator
		implements RequestGeneratorConnectionI{


	public static enum	RGPortTypes {
		INTROSPECTION,
		CONNECTION_IN,
		MANAGEMENT_IN,
		REQUEST_SUBMISSION_OUT,
		REQUEST_NOTIFICATION_IN,
		REQUEST_NOTIFICATION_HANDLER_IN;
	}

	private String rgURI;
	private RequestGeneratorInboundPort rgip;
	
	public RequestGenerator(
			String rgURI,
			String connection_In,
			Double meanInterArrivalTime,
			Long meanNumberOfInstructions,
			String managementInboundPortURI,
			String requestSubmissionOutboundPortURI,
			String requestNotificationInboundPortURI) throws Exception {
		super(rgURI,
				meanInterArrivalTime,
				meanNumberOfInstructions,
				managementInboundPortURI,
				requestSubmissionOutboundPortURI,
				requestNotificationInboundPortURI);
		this.rgURI = rgURI;

		this.rgip = new RequestGeneratorInboundPort(connection_In, this);
		this.addPort(this.rgip);
		this.rgip.publishPort();
		this.addOfferedInterface(RequestGeneratorConnectionI.class);
		
		// Rg debug
		this.toggleTracing();
		this.toggleLogging();
	}
	

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		super.logMessage(rgURI  + " : gettting an answer for " + r.getRequestURI());
		super.acceptRequestTerminationNotification(r);
	}
	
	@Override
	public void startGeneration() throws Exception {
		try {
		super.startGeneration();
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	

	public void addPort(PortI p) throws Exception{
		super.addPort(p);
	}

	public static Map<RGPortTypes, String> newInstance(String rgURI, double meanInterArrivalTime,
			long meanNumberOfInstructions, DynamicComponentCreationI dcc) throws Exception {
		
		String managementInboundPortURI = AbstractPort.generatePortURI();
		String requestNotificationInboundPortURI = AbstractPort.generatePortURI();
		String requestSubmissionOutboundPortURI = AbstractPort.generatePortURI();
		String connection_In = AbstractPort.generatePortURI();
		
		
		Object[] args = new Object[] {
				rgURI,
				connection_In,
				meanInterArrivalTime,
				meanNumberOfInstructions,
				managementInboundPortURI,
				requestSubmissionOutboundPortURI,
				requestNotificationInboundPortURI
		};
		try {
			dcc.createComponent(RequestGenerator.class.getCanonicalName(), args);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		HashMap<RGPortTypes, String> ret =
				new HashMap<RGPortTypes, String>();		
		ret.put(RGPortTypes.INTROSPECTION,
				rgURI);
		ret.put(RGPortTypes.CONNECTION_IN, 
				connection_In);
		ret.put(RGPortTypes.MANAGEMENT_IN,
				managementInboundPortURI) ;
		ret.put(RGPortTypes.REQUEST_SUBMISSION_OUT,
				requestSubmissionOutboundPortURI);
		ret.put(RGPortTypes.REQUEST_NOTIFICATION_IN,
				requestNotificationInboundPortURI);

		return ret;
	}

	@Override
	public void doConnectionWithRD(String RD_Request_Submission_In) throws Exception {
		try {
			
//			System.out.println("[DEBUG LEO] (1) super.rsop.connected() : " + super.rsop.connected());
			
			super.rsop.doConnection(RD_Request_Submission_In, RequestSubmissionConnector.class.getCanonicalName());	

//			System.out.println("[DEBUG LEO] (2) super.rsop.connected() : " + super.rsop.connected());

		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
}
