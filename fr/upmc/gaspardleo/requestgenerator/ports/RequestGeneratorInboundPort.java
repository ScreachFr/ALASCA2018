package fr.upmc.gaspardleo.requestgenerator.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;

/**
 * La classe <code> RequestGeneratorInboundPort </ code> implémente le port entrant 
 * offrant l'interface <code> RequestGeneratorConnectionI </ code>.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		RequestGeneratorInboundPort
		extends 	AbstractInboundPort
		implements 	RequestGeneratorConnectionI {

	private static final long serialVersionUID = 1L;

	/**
	 * @param 	uri			URI de l'inbound port.
	 * @param 	owner		Composant propriétaire du port.
	 * @throws 	Exception
	 */
	public RequestGeneratorInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RequestGeneratorConnectionI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI#doConnectionWithRD(String)
	 */
	@Override
	public void doConnectionWithRD(String RD_Request_Submission_In) throws Exception {
		final RequestGenerator rg = ((RequestGenerator)this.owner);
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<RequestGenerator>(){
					@Override
					public RequestGenerator call() throws Exception {
						rg.doConnectionWithRD(RD_Request_Submission_In);
						return rg;
					}});
	}
}
