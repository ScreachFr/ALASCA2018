package fr.upmc.gaspardleo.componentmanagement;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * L'interface <code> ShutdownableI </ code> définit le comportement des composant
 * pour qu'il s'éteigne. 
 * 
 * @author Leonor & Alexandre
 */
public 	interface 	ShutdownableI 
		extends		OfferedI, 
					RequiredI {
	
	/**
	 * Cette methode permet de demandé au composant qui l'implémente de 
	 * s'éteindre. Dans la majeur partie des cas, elle appelle tout simplement
	 * la méthode shutdown() d'un composant.
	 * @throws Exception
	 */
	public void shutdown() throws Exception;
}
