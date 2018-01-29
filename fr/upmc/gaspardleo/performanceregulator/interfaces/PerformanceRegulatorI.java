package fr.upmc.gaspardleo.performanceregulator.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface PerformanceRegulatorI 
	extends OfferedI, RequiredI{
	
	/**
	 * Demande au ComputerPool connect� d'augmenter la fr�quence des Core
	 * allou� � toutes les ApplicationVM. 
	 * @return
	 * 	A-t-il au moins un Core dont la fr�quence a �t� modifi�e ?
	 * @throws Exception
	 */
	public Boolean increaseCPUFrequency() throws Exception;
	
	/**
	 * Demande au ComputerPool connect� de diminuer la fr�quence des Core
	 * allou� � toutes les ApplicationVM. 
	 * @return
	 * 	A-t-il au moins un Core dont la fr�quence a �t� modifi�e ?
	 * @throws Exception
	 */
	public Boolean decreaseCPUFrequency() throws Exception;
	
	/**
	 * Demande au ComputerPool d'ajouter une ApplicationVM au RequestDispatcher.
	 * @return
	 * 		Est-ce-qu'une ApplicationVM a �t� ajout�e ?
	 * 	Un retour n�gatif peut �tre caus� par une abscence de ressource
	 * disponible.
	 * @throws Exception
	 */
	public Boolean addAVMToRD() throws Exception;
	
	/**
	 * Demande au ComputerPool de retirer une ApplicationVM au RequestDispatcher.
	 * @return
	 * 		Est-ce-qu'une ApplicationVM a �t� retir�e ?
	 * Retournera false lorsqu'il ne reste plus qu'une ApplicationVM allou�e au RequestDispatcher.
	 * @throws Exception
	 */
	public Boolean removeAVMFromRD() throws Exception;
	
	/**
	 * Permet de definir la strategie de regulation du PerformanceRegulator.
	 * @param strat
	 * 		Nouvelle strat�gie � adopter.
	 * @throws Exception
	 */
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception;
	
	/**
	 * Permet d'obtenir la strategie actuellement utilis�e par le PerformanceRegulator.
	 * @return
	 * 		Strat�gie actuellement utilis�e.
	 * @throws Exception
	 */
	public RegulationStrategyI getRegulationStrategie() throws Exception;
	
	/**
	 * Lance la boucle de r�gulation. Cela implique que d�s que cette m�thode a �t� appel�, le 
	 * RequestDispatcher associ� sera alors soumis au control et r�gulation des performance
	 * offert par ce PerformanceRegulator.
	 * Cette m�thode doit lanc� le control periodique. Elle ne devra �tre appel�e qu'une seule fois.
	 * @throws Exception
	 */
	public void startRegulationControlLoop() throws Exception;
}
