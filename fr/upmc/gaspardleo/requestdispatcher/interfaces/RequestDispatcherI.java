package fr.upmc.gaspardleo.requestdispatcher.interfaces;

import java.util.HashMap;
import java.util.List;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;

/**
 * L'interface <code> RequestDispatcherI </ code> définit le comportement de l'objet RequestDispatcher
 * pour la répartition de requêtes dans le centre de calcul.
 * 
 * @author Leonor & Alexandre
 */
public 	interface 	RequestDispatcherI
		extends		OfferedI, 
					RequiredI {

	/**
	 * Connect une application VM au RequestDispatcher.
	 * @param 	vmUri 							Uri de l'application VM.
	 * @param 	requestSubmissionOutboundPort 	Uri du port de soumission des requètes.
	 * @return 	Uri du port de notification de terminaison des requètes.
	 * @throws 	Exception
	 */
	public String registerVM(
			HashMap<ApplicationVMPortTypes, String> avmURIs, 
			Class<?> vmInterface) throws Exception;
	
	/**
	 * Deconnecte une ApplicationVM du RequestDispatcher. 
	 * @param 	vmUri 		Uri de composant de l'application VM à deconnecter.
	 * @throws 	Exception
	 */
	public void unregisterVM(String vmUri) throws Exception;
	
	/**
	 * Deconnecte une ApplicationVM du RequestDispatcher.
	 * Il es probable que cette methode deconnecte uneapplication VM aléatoire.
	 * @throws Exception
	 */
	public void unregisterVM() throws Exception;
	
	/**
	 * Permet de disposer de la liste des ApplicationVM utilisées par le RequestDispatcher.
	 * @return Liste des URI de composant des ApplicationVM.
	 * @throws Exception
	 */
	public List<String> getRegisteredAVMUris() throws Exception;
}
