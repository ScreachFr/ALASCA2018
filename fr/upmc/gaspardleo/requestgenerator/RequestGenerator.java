package fr.upmc.gaspardleo.requestgenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreator;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.software.interfaces.RequestI;

public class RequestGenerator 
extends fr.upmc.datacenterclient.requestgenerator.RequestGenerator{


	public static enum	RGPortTypes {
		INTROSPECTION,
		MANAGEMENT_IN,
		REQUEST_SUBMISSION_OUT,
		REQUEST_NOTIFICATION_IN,
		REQUEST_NOTIFICATION_HANDLER_IN;
	}

	private String rgURI;

	public RequestGenerator(String rgURI,
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

		// Rg debug
		this.toggleTracing();
		this.toggleLogging();

	}



	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		super.logMessage(rgURI  + " : gettting an answer for " + r.getRequestURI());
		System.out.println(rgURI  + " : gettting an answer for " + r.getRequestURI());
		super.acceptRequestTerminationNotification(r);
	}
	

	public void addPort(PortI p) throws Exception{
		super.addPort(p);
	}

	public static Map<RGPortTypes, String> newInstance(String rgURI, double meanInterArrivalTime,
			long meanNumberOfInstructions, DynamicComponentCreationI dcc) throws Exception {
		
		String managementInboundPortURI = AbstractPort.generatePortURI();
		String requestNotificationInboundPortURI = AbstractPort.generatePortURI();
		String requestSubmissionOutboundPortURI = AbstractPort.generatePortURI();
		
		
		
		Object[] args = new Object[] {
				rgURI,
				meanInterArrivalTime,
				meanNumberOfInstructions,
				managementInboundPortURI,
				requestSubmissionOutboundPortURI,
				requestNotificationInboundPortURI
		};

		dcc.createComponent(RequestGenerator.class.getCanonicalName(), args);

		HashMap<RGPortTypes, String> ret =
				new HashMap<RGPortTypes, String>();		
		ret.put(RGPortTypes.INTROSPECTION,
				rgURI);
		ret.put(RGPortTypes.MANAGEMENT_IN,
				managementInboundPortURI) ;
		ret.put(RGPortTypes.REQUEST_SUBMISSION_OUT,
				requestSubmissionOutboundPortURI);
		ret.put(RGPortTypes.REQUEST_NOTIFICATION_IN,
				requestNotificationInboundPortURI);

		return ret;
	}
}
