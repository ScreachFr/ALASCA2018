package fr.upmc.gaspardleo.performanceregulator.interfaces;

public interface PerformanceRegulatorI {
	
	
	public Boolean increaseCPUFrequency() throws Exception;
	
	public Boolean decreaseCPUFrequency() throws Exception;
	
	public Boolean addAVMToRD() throws Exception;
	
	public Boolean removeAVMFromRD() throws Exception;
	
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception;

	public RegulationStrategyI getRegulationStrategie() throws Exception;
	
	public void startRegulationControlLoop() throws Exception;
}
