package fr.upmc.gaspardleo.performanceregulator.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface RegulationStrategyI 
		extends	OfferedI, RequiredI{
	
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception;
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception;
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception;
}
