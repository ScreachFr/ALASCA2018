package fr.upmc.gaspardleo.requestgenerator;

import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.ports.AbstractPort;

public class RequestGenerator 
	extends fr.upmc.datacenterclient.requestgenerator.RequestGenerator{

	public static enum	RGPortTypes {
		INTROSECTION, MANAGEMENT_IN, REQUEST_SUBMISSION_OUT, REQUEST_NOTIFICATION_IN
	}
	
	private static double MEAN_INTER_ARRIVAL_TIME 	= 500.0;
	private static long MEAN_NUMBER_OF_INSTRUCTIONS = 6000000000L;
	
	private static String rg_rgmip 	= AbstractPort.generatePortURI();
	private static String rg_rsop 	= AbstractPort.generatePortURI();
	private static String rg_rnip 	= AbstractPort.generatePortURI();
	
	private String rgURI;
	
	public RequestGenerator(
			String rgURI) throws Exception {
		
		super(
			rgURI, 
			MEAN_INTER_ARRIVAL_TIME, 
			MEAN_NUMBER_OF_INSTRUCTIONS, 
			rg_rgmip,
			rg_rsop,
			rg_rnip);
		
		this.rgURI = rgURI;
	}

	public Map<RGPortTypes, String>	getRGPortsURI() throws Exception {
		HashMap<RGPortTypes, String> ret =
				new HashMap<RGPortTypes, String>();		
		ret.put(RGPortTypes.INTROSECTION,
				this.rgURI);
		ret.put(RGPortTypes.MANAGEMENT_IN,
				RequestGenerator.rg_rgmip) ;
		ret.put(RGPortTypes.REQUEST_SUBMISSION_OUT,
				RequestGenerator.rg_rsop);
		ret.put(RGPortTypes.REQUEST_NOTIFICATION_IN,
				RequestGenerator.rg_rnip);
		return ret ;
	}
}
