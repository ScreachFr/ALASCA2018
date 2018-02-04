package fr.upmc.gaspardleo.requestgenerator;

import java.util.HashMap;

import fr.upmc.components.ports.AbstractPort;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;
import fr.upmc.gaspardleo.requestgenerator.ports.RequestGeneratorInboundPort;

/**
 * La classe <code> RequestGenerator </ code> implémente le composant représentant 
 * le générateur requêts dans le centre de calcul.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		RequestGenerator 
		extends 	fr.upmc.datacenterclient.requestgenerator.RequestGenerator
		implements 	RequestGeneratorConnectionI {

	public static enum	RGPortTypes {
		INTROSPECTION,
		CONNECTION_IN,
		MANAGEMENT_IN,
		REQUEST_SUBMISSION_OUT,
		REQUEST_NOTIFICATION_IN,
		REQUEST_NOTIFICATION_HANDLER_IN
	}

	/** Inboud port offrant les services du RequestGenerator */
	private RequestGeneratorInboundPort rgip;
	
	/**
	 * @param 	rg_uris						URIs du composant
	 * @param 	meanInterArrivalTime		Temps interarrivaire moyen des requêtes en ms.
	 * @param 	meanNumberOfInstructions	Nombre moyen d'instructions des requêtes en ms.
	 * @throws 	Exception
	 */
	public RequestGenerator(
		HashMap<RGPortTypes, String> rg_uris,
		Double meanInterArrivalTime,
		Long meanNumberOfInstructions) throws Exception {
		
		super(
			rg_uris.get(RGPortTypes.INTROSPECTION),
			meanInterArrivalTime,
			meanNumberOfInstructions,
			rg_uris.get(RGPortTypes.MANAGEMENT_IN),
			rg_uris.get(RGPortTypes.REQUEST_SUBMISSION_OUT),
			rg_uris.get(RGPortTypes.REQUEST_NOTIFICATION_IN));
		
		this.addOfferedInterface(RequestGeneratorConnectionI.class);
		this.rgip = new RequestGeneratorInboundPort(rg_uris.get(RGPortTypes.CONNECTION_IN), this);
		this.addPort(this.rgip);
		this.rgip.publishPort();
		
		// Rg debug
		this.toggleTracing();
		this.toggleLogging();
		this.logMessage("RequestGenerator made");
	}

	/**
	 * @see fr.upmc.datacenterclient.requestgenerator.RequestGenerator#acceptRequestTerminationNotification(RequestI)
	 */
	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		super.logMessage(rgURI  + " : gettting an answer for " + r.getRequestURI());
		super.acceptRequestTerminationNotification(r);
	}
	
	/**
	 * @see fr.upmc.datacenterclient.requestgenerator.RequestGenerator#startGeneration()
	 */
	@Override
	public void startGeneration() throws Exception {
		try {
			super.startGeneration();
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Ajoute un port au composant
	 */
	public void addPort(PortI p) throws Exception{
		super.addPort(p);
	}

	/**
	 * @see fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI#doConnectionWithRD(String)
	 */
	@Override
	public void doConnectionWithRD(String RD_Request_Submission_In) throws Exception {
		
		RequestSubmissionOutboundPort rsop = 
			(RequestSubmissionOutboundPort) super.findPortFromURI(super.requestSubmissionOutboundPortURI);
		
		if (rsop == null){
			if(!this.isRequiredInterface(RequestSubmissionI.class))
				super.addRequiredInterface(RequestSubmissionI.class) ;
			
			rsop = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI, this) ;
			super.addPort(rsop) ;
			rsop.publishPort() ;
		}
		
		rsop.doConnection(
			RD_Request_Submission_In, 
			ClassFactory.newConnector(RequestSubmissionI.class).getCanonicalName());
	}

	/**
	 * Construit les URIs du composant et de ses ports
	 * @param 	num_rg	Numéro du composant permettant la création unique d'URI
	 * @return	Les URIs du composant et de ses ports
	 */
	public static HashMap<RGPortTypes, String> makeUris(int num_rg){
		HashMap<RGPortTypes, String> rg_uris = new HashMap<RGPortTypes, String>();		
		rg_uris.put(RGPortTypes.INTROSPECTION, "rg-"+num_rg);
		rg_uris.put(RGPortTypes.CONNECTION_IN, AbstractPort.generatePortURI());
		rg_uris.put(RGPortTypes.MANAGEMENT_IN, AbstractPort.generatePortURI()) ;
		rg_uris.put(RGPortTypes.REQUEST_SUBMISSION_OUT, AbstractPort.generatePortURI());
		rg_uris.put(RGPortTypes.REQUEST_NOTIFICATION_IN, AbstractPort.generatePortURI());
		return rg_uris;
	}
}
