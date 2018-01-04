package fr.upmc.gaspardleo.performanceregulator.interfaces;

public interface RegulationStrategyI {
	
	public void increasePerformances(PerformanceRegulatorI regulator) throws Exception;
	
	public void decreasePerformances(PerformanceRegulatorI regulator) throws Exception;
	
	public Boolean canRegulate(PerformanceRegulatorI regulator) throws Exception;
}
