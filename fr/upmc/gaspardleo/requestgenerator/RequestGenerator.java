package fr.upmc.gaspardleo.requestgenerator;

import java.util.HashMap;

import fr.upmc.components.ports.AbstractPort;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;
import fr.upmc.gaspardleo.requestgenerator.ports.RequestGeneratorInboundPort;

public 	class RequestGenerator 
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

	private RequestGeneratorInboundPort rgip;
	
	public RequestGenerator(
			HashMap<RGPortTypes, String> rg_uris,
			Double meanInterArrivalTime,
			Long meanNumberOfInstructions
			) throws Exception {
		
		super(
			rg_uris.get(RGPortTypes.INTROSPECTION),
			meanInterArrivalTime,
			meanNumberOfInstructions,
			rg_uris.get(RGPortTypes.MANAGEMENT_IN),
			rg_uris.get(RGPortTypes.REQUEST_SUBMISSION_OUT),
			rg_uris.get(RGPortTypes.REQUEST_NOTIFICATION_IN)
		);
		
		this.addOfferedInterface(RequestGeneratorConnectionI.class);
		this.rgip = new RequestGeneratorInboundPort(rg_uris.get(RGPortTypes.CONNECTION_IN), this);
		this.addPort(this.rgip);
		this.rgip.publishPort();
		
		// Rg debug
		this.toggleTracing();
		this.toggleLogging();
		
		this.logMessage("RequestGenerator made");
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

	@Override
	public void doConnectionWithRD(String RD_Request_Submission_In) throws Exception {
		
		RequestSubmissionOutboundPort rsop = 
				(RequestSubmissionOutboundPort) super.findPortFromURI(super.requestSubmissionOutboundPortURI);
		
		if (rsop == null){
			super.addRequiredInterface(RequestSubmissionI.class) ;
			rsop = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI, this) ;
			super.addPort(rsop) ;
			rsop.publishPort() ;
		}

		try {
				
			rsop.doConnection(
					RD_Request_Submission_In, 
					RequestSubmissionConnector.class.getCanonicalName());	

		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	public static HashMap<RGPortTypes, String> newInstance(
			String rgURI, 
			Double meanInterArrivalTime,
			Long meanNumberOfInstructions,
			ComponentCreator cc) throws Exception {
		
		HashMap<RGPortTypes, String> rg_uris = new HashMap<RGPortTypes, String>();		
		rg_uris.put(RGPortTypes.INTROSPECTION, rgURI);
		rg_uris.put(RGPortTypes.CONNECTION_IN, AbstractPort.generatePortURI());
		rg_uris.put(RGPortTypes.MANAGEMENT_IN, AbstractPort.generatePortURI()) ;
		rg_uris.put(RGPortTypes.REQUEST_SUBMISSION_OUT, AbstractPort.generatePortURI());
		rg_uris.put(RGPortTypes.REQUEST_NOTIFICATION_IN, AbstractPort.generatePortURI());
		
		/* Constructeur :
		 
		  	HashMap<RGPortTypes, String> rg_uris,
			Double meanInterArrivalTime,
			Long meanNumberOfInstructions
		 */
		Object[] constructorParams = new Object[] {
				rg_uris,
				meanInterArrivalTime,
				meanNumberOfInstructions
		};
		
		try {
			cc.createComponent(RequestGenerator.class, constructorParams);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}

		return rg_uris;
	}
}
