package fr.upmc.gaspardleo.performanceregulator.interfaces;

public interface PerformanceRegulatorI {
	
	/**
	 * Demande au ComputerPool connecté d'augmenter la fréquence des Core
	 * alloué à toutes les ApplicationVM. 
	 * @return
	 * 	A-t-il au moins un Core dont la fréquence a été modifiée ?
	 * @throws Exception
	 */
	public Boolean increaseCPUFrequency() throws Exception;
	
	/**
	 * Demande au ComputerPool connecté de diminuer la fréquence des Core
	 * alloué à toutes les ApplicationVM. 
	 * @return
	 * 	A-t-il au moins un Core dont la fréquence a été modifiée ?
	 * @throws Exception
	 */
	public Boolean decreaseCPUFrequency() throws Exception;
	
	/**
	 * Demande au ComputerPool d'ajouter une ApplicationVM au RequestDispatcher.
	 * @return
	 * 		Est-ce-qu'une ApplicationVM a été ajoutée ?
	 * 	Un retour négatif peut être causé par une abscence de ressource
	 * disponible.
	 * @throws Exception
	 */
	public Boolean addAVMToRD() throws Exception;
	
	/**
	 * Demande au ComputerPool de retirer une ApplicationVM au RequestDispatcher.
	 * @return
	 * 		Est-ce-qu'une ApplicationVM a été retirée ?
	 * Retournera false lorsqu'il ne reste plus qu'une ApplicationVM allouée au RequestDispatcher.
	 * @throws Exception
	 */
	public Boolean removeAVMFromRD() throws Exception;
	
	/**
	 * Permet de definir la strategie de regulation du PerformanceRegulator.
	 * @param strat
	 * 		Nouvelle stratégie à adopter.
	 * @throws Exception
	 */
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception;
	
	/**
	 * Permet d'obtenir la strategie actuellement utilisée par le PerformanceRegulator.
	 * @return
	 * 		Stratégie actuellement utilisée.
	 * @throws Exception
	 */
	public RegulationStrategyI getRegulationStrategie() throws Exception;
	
	/**
	 * Lance la boucle de régulation. Cela implique que dès que cette méthode a été appelé, le 
	 * RequestDispatcher associé sera alors soumis au control et régulation des performance
	 * offert par ce PerformanceRegulator.
	 * Cette méthode doit lancé le control periodique. Elle ne devra être appelée qu'une seule fois.
	 * @throws Exception
	 */
	public void startRegulationControlLoop() throws Exception;
}
