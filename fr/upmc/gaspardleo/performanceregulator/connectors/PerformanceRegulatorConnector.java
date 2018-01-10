package fr.upmc.gaspardleo.performanceregulator.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

public class PerformanceRegulatorConnector 
	extends AbstractConnector
	implements PerformanceRegulatorI{

	@Override
	public Boolean increaseCPUFrequency() throws Exception {
		return ((PerformanceRegulatorI)this.offering).increaseCPUFrequency();
	}

	@Override
	public Boolean decreaseCPUFrequency() throws Exception {
		return ((PerformanceRegulatorI)this.offering).decreaseCPUFrequency();
	}

	@Override
	public Boolean addAVMToRD() throws Exception {
		return ((PerformanceRegulatorI)this.offering).addAVMToRD();
	}

	@Override
	public Boolean removeAVMFromRD() throws Exception {
		return ((PerformanceRegulatorI)this.offering).removeAVMFromRD();
	}

	@Override
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception {
		((PerformanceRegulatorI)this.offering).setRegulationStrategie(strat);
	}

	@Override
	public RegulationStrategyI getRegulationStrategie() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startRegulationControlLoop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
