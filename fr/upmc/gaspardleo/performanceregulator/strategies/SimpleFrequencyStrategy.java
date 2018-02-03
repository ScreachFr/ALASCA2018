package fr.upmc.gaspardleo.performanceregulator.strategies;

import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

/**
 * La classe <code> SimpleFrequencyStrategy </ code> implémente le comportement
 * pour une stratégie de régulation via les fréquences
 * 
 * @author Leonor & Alexandre
 */
public 	class 		SimpleFrequencyStrategy 
		implements 	RegulationStrategyI{
	
	/** Varibale pour l'activation du mode débug */
	private Boolean DEBUG = true;
	/** Permet de savoir si la régulation est autorisée */
	private Boolean canRegulate;	
	
	public SimpleFrequencyStrategy() {
		this.canRegulate = true;
	}
	
	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#increasePerformances(PerformanceRegulatorI)
	 */
	@Override
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception {
		if (canRegulate) {
			canRegulate = regulator.increaseCPUFrequency();
			if (DEBUG) {
				if (canRegulate)
					System.out.println("CPU frequency has been increased for at least one core.");
				else
					System.out.println("CPU frequency has been maxed out!");
			}
		}
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#decreasePerformances(PerformanceRegulatorI)
	 */
	@Override
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception {
		regulator.decreaseCPUFrequency();
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#canRegulate(PerformanceRegulatorI)
	 */
	@Override
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception {
		return canRegulate;
	}
}
