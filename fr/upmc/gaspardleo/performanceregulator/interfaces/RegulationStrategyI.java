package fr.upmc.gaspardleo.performanceregulator.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface RegulationStrategyI 
		extends	OfferedI, RequiredI{
	
	/**
	 * L'appel de cette m�thode doit permettre de r�duire le temps d'attente
	 * entre le traitement de chaque requ�te du RequestDispatcher associ�.
	 * @param regulator
	 * 		R�gulateur qui sur lequel les changement doivent �tre op�r�s.
	 * @throws Exception
	 */
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception;
	
	/**
	 * L'appel de cette m�thode doit permettre d'augmenter le temps d'attente
	 * entre le traitement de chaque requ�te du RequestDispatcher associ�.
	 * @param regulator
	 * 		R�gulateur qui sur lequel les changement doivent �tre op�r�s.
	 * @throws Exception
	 */
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception;
	
	/**
	 * Indique si une r�gulation peut �tre op�r�.
	 * @param regulator
	 * 		R�gulateur qui sur lequel les changement doivent �tre op�r�s.
	 * @return
	 * 		Une r�gulation de performance peut-elle �tre eff�ctu�e ?
	 * @throws Exception
	 */
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception;
}
