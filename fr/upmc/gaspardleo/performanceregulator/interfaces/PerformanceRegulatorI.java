package fr.upmc.gaspardleo.performanceregulator.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * L'interface <code> PerformanceRegulatorI </ code> définit le comportement de l'objet PerformanceRegulator
 * pour l'adaptation du centre de calcul
 * @author Leonor & Alexandre
 */
public interface PerformanceRegulatorI 
	extends OfferedI, RequiredI{
	
	/**
	 * Demande au ComputerPool connecté d'augmenter la fréquence des coeurs
	 * alloués à toutes les applications VM. 
	 * @return 	A-t-il au moins un Core dont la fréquence a été modifiée ?
	 * @throws 	Exception
	 */
	public Boolean increaseCPUFrequency() throws Exception;
	
	/**
	 * Demande au ComputerPool connecté de diminuer la fréquence des coeurs
	 * alloués à toutes les applications VM. 
	 * @return	A-t-il au moins un coeur dont la fréquence a été modifiée ?
	 * @throws 	Exception
	 */
	public Boolean decreaseCPUFrequency() throws Exception;
	
	/**
	 * Demande au ComputerPool d'ajouter une application VM au RequestDispatcher.
	 * @return	Est-ce-qu'une application VM a été ajoutée ?
	 * 			Un retour négatif peut être causé par une abscence de ressource
	 * 			disponible.
	 * @throws 	Exception
	 */
	public Boolean addAVMToRD() throws Exception;
	
	/**
	 * Demande au ComputerPool de retirer une ApplicationVM au RequestDispatcher.
	 * @return	Est-ce-qu'une application VM a été retirée ?
	 * 			Retournera false lorsqu'il ne reste plus qu'une application VM allouée au RequestDispatcher.
	 * @throws 	Exception
	 */
	public Boolean removeAVMFromRD() throws Exception;
	
	/**
	 * Définie la strategie de regulation du PerformanceRegulator.
	 * @param	strat		Nouvelle stratégie à adopter.
	 * @throws	Exception
	 */
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception;
	
	/**
	 * Retourne la strategie actuellement utilisée par le PerformanceRegulator.
	 * @return	Stratégie actuellement utilisée.
	 * @throws 	Exception
	 */
	public RegulationStrategyI getRegulationStrategie() throws Exception;
	
	/**
	 * Lance la boucle de régulation. Cela implique que dès que cette méthode a été appelée, le 
	 * RequestDispatcher associé sera alors soumis au contrôl et régulation des performances
	 * offerttes par ce PerformanceRegulator.
	 * Cette méthode doit lancer le contrôl periodique. Elle ne devra être appelée qu'une seule fois.
	 * @throws Exception
	 */
	public void startRegulationControlLoop() throws Exception;
}
