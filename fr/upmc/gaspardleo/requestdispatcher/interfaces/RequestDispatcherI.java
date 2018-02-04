package fr.upmc.gaspardleo.requestdispatcher.interfaces;

import java.util.HashMap;
import java.util.List;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;

/**
 * L'interface <code> RequestDispatcherI </ code> définit le comportement de l'objet RequestDispatcher
 * pour la répartition de requêtes dans le centre de calcul.
 * @author Leonor & Alexandre
 */
public interface RequestDispatcherI
		extends	OfferedI, RequiredI{

	/**
	 * Connect une ApplicationVM au RequestDispatcher.
	 * @param 	vmUri 							Uri de l'AVM.
	 * @param 	requestSubmissionOutboundPort 	Uri du port de soumission des requètes.
	 * @return 	Uri du port de notification de terminaison des requètes.
	 * @throws 	Exception
	 */
	public String registerVM(
			HashMap<ApplicationVMPortTypes, String> avmURIs, 
			Class<?> vmInterface) throws Exception;
	
	/**
	 * Deconnecte une ApplicationVM du RequestDispatcher. 
	 * @param 	vmUri 		Uri de composant de l'ApplicationVM � deconnecter.
	 * @throws 	Exception
	 */
	public void unregisterVM(String vmUri) throws Exception;
	
	/**
	 * Deconnect une ApplicationVM du RequestDispatcher.
	 * Il es probable que cette methode deconnecte une ApplicationVM al�atoire.
	 * @throws Exception
	 */
	public void unregisterVM() throws Exception;
	
	/**
	 * Permet de disposer de la liste des ApplicationVM utilis�es par le RequestDispatcher.
	 * @return Liste des URI de composant des ApplicationVM.
	 * @throws Exception
	 */
	public List<String> getRegisteredAVMUris() throws Exception;
}
