package fr.upmc.gaspardleo.requestgenerator.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;

public class RequestGeneratorOutboundPort 
	extends AbstractOutboundPort
	implements RequestGeneratorConnectionI{
	
	public RequestGeneratorOutboundPort(ComponentI owner) throws Exception {
		super(RequestGeneratorConnectionI.class, owner);
	}

	@Override
	public void doConnectionWithRD(String RD_Request_Submission_In) throws Exception {
		((RequestGeneratorConnectionI)this.connector).doConnectionWithRD(RD_Request_Submission_In);
	}

}
