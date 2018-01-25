package fr.upmc.gaspardleo.performanceregulator.interfaces;

public interface RegulationStrategyI {
	
	/**
	 * L'appel de cette méthode doit permettre de réduire le temps d'attente
	 * entre le traitement de chaque requête du RequestDispatcher associé.
	 * @param regulator
	 * 		Régulateur qui sur lequel les changement doivent être opérés.
	 * @throws Exception
	 */
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception;
	
	/**
	 * L'appel de cette méthode doit permettre d'augmenter le temps d'attente
	 * entre le traitement de chaque requête du RequestDispatcher associé.
	 * @param regulator
	 * 		Régulateur qui sur lequel les changement doivent être opérés.
	 * @throws Exception
	 */
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception;
	
	/**
	 * Indique si une régulation peut être opéré.
	 * @param regulator
	 * 		Régulateur qui sur lequel les changement doivent être opérés.
	 * @return
	 * 		Une régulation de performance peut-elle être efféctuée ?
	 * @throws Exception
	 */
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception;
}
