package fr.upmc.gaspardleo.requestgenerator.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface RequestGeneratorConnectionI 
	extends	OfferedI, RequiredI{

	/**
	 * Connecte un RequestGenerator avec une interface de traitement de requête.
	 * Le port utilisé ici est celui de RequestSubmission. Il permet au 
	 * RequestGenerator d'avoir une réponse à ses requêtes.
	 * ie : RequestDispatcher/ApplicationVM in -> RequestGenerator_out.
	 * @param Request_Submission_In
	 * 		Uri du RequestSubmissionInboundPort, donc server, avec lequel se connecter.
	 * @throws Exception
	 */
	public void doConnectionWithRD(String Request_Submission_In) throws Exception;
}
