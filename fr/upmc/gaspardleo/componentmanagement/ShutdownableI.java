package fr.upmc.gaspardleo.componentmanagement;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface ShutdownableI 
		extends	OfferedI, RequiredI{
	
	/**
	 * Cette methode permet de demand� au composant qui l'impl�mente de 
	 * s'�teindre. Dans la majeur partie des cas, elle appelle tout simplement
	 * la m�thode shutdown() d'un composant.
	 * @throws Exception
	 */
	public void shutdown() throws Exception;
}
