package fr.upmc.gaspardleo.performanceregulator.strategies;

import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

public class SimpleFrequencyStrategy implements RegulationStrategyI{
	private Boolean DEBUG = true;
	
	private Boolean canRegulate;	
	
	public SimpleFrequencyStrategy() {
		this.canRegulate = true;
	}
	
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

	@Override
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception {
		regulator.decreaseCPUFrequency();
	}

	@Override
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception {
		return canRegulate;
	}

}
