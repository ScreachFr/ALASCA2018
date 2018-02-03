package fr.upmc.gaspardleo.performanceregulator.strategies;

import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

/**
 * La classe <code> SimpleAVMStrategie </ code> implémente le comportement
 * pour une stratégie de régulation via les applications VM.
 * 
 * @author Leonor & Alexandre
 */
public class 	SimpleAVMStrategie 
	implements 	RegulationStrategyI {
	
	/** Permet de savoir si la régulation est autorisée */
	private Boolean canRegulate;

	public SimpleAVMStrategie() {
		this.canRegulate = true;
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#increasePerformances(PerformanceRegulatorI)
	 */
	@Override
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception {
		if (!regulator.addAVMToRD()) {
			System.out.println("SimpleAVMStrategy : Cannot add any AVM, there's no ressources available at the moment.");
			canRegulate = false;
		} 
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#decreasePerformances(PerformanceRegulatorI)
	 */
	@Override
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception {
		regulator.removeAVMFromRD();
		canRegulate = true;
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#canRegulate(PerformanceRegulatorI)
	 */
	@Override
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception {
		return canRegulate; 
	}
}
