package fr.upmc.gaspardleo.performanceregulator.strategies;

import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

/**
 * Cette classe représente une stratégie de régulation qui influt sur la fréquence des processeur 
 * ainsi que le nombre d'AVM. En effet elle va d'abord essayer d'augmenter ou de reduire la fréquence 
 * des processeurs concernés avant d'allourer ou de desallouer des AVMs.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		AVMAndFrequencyStrategy 
		implements 	RegulationStrategyI {

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI#increasePerformances(PerformanceRegulatorI)
	 */
	@Override
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception {
		if (!regulator.increaseCPUFrequency()) {
			System.out.println("Regulation strategy : looks like the CPU frequency has alreaddy been maxed out. Will try to add an AVM.");
			if (!regulator.addAVMToRD()) 
				System.out.println("Regulation strategy : No available avm !");
			else 
				System.out.println("Regulation strategy : An AVM has been added.");
			
		} else {
			System.out.println("Regulation strategy : Some CPU has been upclocked.");
		}
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI#decreasePerformances(PerformanceRegulatorI)
	 */
	@Override
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception {
		if (!regulator.decreaseCPUFrequency()) {
			System.out.println("Regulation strategy : looks like the CPU frequency is already at its lowest. Will try to remove an AVM.");
			if (!regulator.removeAVMFromRD()) 
				System.out.println("Regulation strategy : Can't remove any AVM since there's only one left in the RD! ");
			else 
				System.out.println("Regulation strategy : An AVM has been removed.");
		} else {
			System.out.println("Regulation strategy : Some CPU has been downclocked.");
		}
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI#canRegulate(PerformanceRegulatorI)
	 */
	@Override
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception {
		return true;
	}
}
