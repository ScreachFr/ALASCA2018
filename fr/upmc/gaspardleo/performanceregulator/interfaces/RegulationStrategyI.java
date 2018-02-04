package fr.upmc.gaspardleo.performanceregulator.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * L'interface <code> RegulationStrategyI </ code> définit les stratégies d'adaptation 
 * du composant PerformanceRegulatorI
 * @author Leonor & Alexandre
 */
public 	interface RegulationStrategyI 
		extends	OfferedI, RequiredI{
	
	/**
	 * Permet de réduire le temps d'attente entre le traitement de chaque requête du RequestDispatcher associé.
	 * @param 	regulator	Régulateur pour lequel les changements doivent être opérés.
	 * @throws 	Exception
	 */
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception;
	
	/**
	 * Permet d'augmenter le temps d'attente entre le traitement de chaque requête du RequestDispatcher associé.
	 * @param 	regulator 	Régulateur pour lequel les changements doivent être opérés.
	 * @throws 	Exception
	 */
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception;
	
	/**
	 * Indique si une régulation peut être opéré.
	 * @param 	regulator 	Régulateur pour lequel les changements doivent être opérés.
	 * @return 	Une régulation de performance peut-elle être efféctuée ?
	 * @throws 	Exception
	 */
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception;
}
