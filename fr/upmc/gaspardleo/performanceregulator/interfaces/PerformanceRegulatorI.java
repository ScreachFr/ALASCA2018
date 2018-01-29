package fr.upmc.gaspardleo.performanceregulator.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface PerformanceRegulatorI
		extends OfferedI, RequiredI {
	
	public Boolean increaseCPUFrequency() throws Exception;
	public Boolean decreaseCPUFrequency() throws Exception;
	public Boolean addAVMToRD() throws Exception;
	public Boolean removeAVMFromRD() throws Exception;
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception;
	public RegulationStrategyI getRegulationStrategie() throws Exception;
	public void startRegulationControlLoop() throws Exception;
}
