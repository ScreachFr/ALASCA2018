package fr.upmc.gaspardleo.requestgenerator.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface RequestGeneratorConnectionI 
	extends	OfferedI, RequiredI{

	public void doConnectionWithRD(String RD_Request_Submission_In) throws Exception;
}
