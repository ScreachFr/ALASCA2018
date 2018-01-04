package fr.upmc.gaspardleo.performanceregulator.strategies;

import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

public class SimpleAVMStrategie implements RegulationStrategyI {
	
	@Override
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception {
		regulator.addAVMToRD();
	}

	@Override
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception {
		regulator.removeAVMFromRD();
	}

	@Override
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception {
		return true; // XXX valeur pas défaut pas forcement correcte.
	}


}
