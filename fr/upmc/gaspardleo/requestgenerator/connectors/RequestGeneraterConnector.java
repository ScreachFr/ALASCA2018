package fr.upmc.gaspardleo.requestgenerator.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;

public class RequestGeneraterConnector 
		extends AbstractConnector
		implements RequestGeneratorConnectionI{

	@Override
	public void doConnectionWithRD(String RD_Request_Submission_In) throws Exception {
		((RequestGeneratorConnectionI)this.offering).doConnectionWithRD(RD_Request_Submission_In);
	}

}
