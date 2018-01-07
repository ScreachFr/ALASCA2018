package fr.upmc.gaspardleo.performanceregulator.strategies;

import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

public class SimpleAVMStrategie implements RegulationStrategyI {
	private Boolean canRegulate;


	public SimpleAVMStrategie() {
		this.canRegulate = true;
	}


	@Override
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception {
		if (!regulator.addAVMToRD()) {
			System.out.println("SimpleAVMStrategy : Cannot add any AVM, there's no ressources available at the moment.");
			canRegulate = false;
		} 

	}

	@Override
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception {
		regulator.removeAVMFromRD();
		canRegulate = true;
	}

	@Override
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception {
		return canRegulate; 
	}


}
