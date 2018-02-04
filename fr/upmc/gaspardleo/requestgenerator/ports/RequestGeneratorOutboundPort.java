package fr.upmc.gaspardleo.requestgenerator.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;

/**
 * La classe <code> RequestGeneratorInboundPort </ code> implémente le port sortrant 
 * offrant l'interface <code> RequestGeneratorConnectionI </ code>.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		RequestGeneratorOutboundPort 
		extends 	AbstractOutboundPort
		implements 	RequestGeneratorConnectionI {

	private static final long serialVersionUID = 1L;

	/**
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public RequestGeneratorOutboundPort(ComponentI owner) throws Exception {
		super(RequestGeneratorConnectionI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI#doConnectionWithRD(String)
	 */
	@Override
	public void doConnectionWithRD(String RD_Request_Submission_In) throws Exception {
		((RequestGeneratorConnectionI)this.connector).doConnectionWithRD(RD_Request_Submission_In);
	}

}
