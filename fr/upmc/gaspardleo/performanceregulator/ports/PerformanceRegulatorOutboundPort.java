package fr.upmc.gaspardleo.performanceregulator.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

public class PerformanceRegulatorOutboundPort 
extends AbstractOutboundPort 
implements PerformanceRegulatorI {

	public PerformanceRegulatorOutboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, PerformanceRegulatorI.class, owner);
	}

	public PerformanceRegulatorOutboundPort(ComponentI owner)
			throws Exception {
		super(PerformanceRegulatorI.class, owner);
	}
	
	@Override
	public Boolean increaseCPUFrequency() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).increaseCPUFrequency();
	}

	@Override
	public Boolean decreaseCPUFrequency() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).decreaseCPUFrequency();
	}

	@Override
	public Boolean addAVMToRD() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).addAVMToRD();
	}

	@Override
	public Boolean removeAVMFromRD() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).removeAVMFromRD();
	}

	@Override
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception {
		((PerformanceRegulatorI)(this.connector)).setRegulationStrategie(strat);
	}

	@Override
	public RegulationStrategyI getRegulationStrategie() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).getRegulationStrategie();
	}

	@Override
	public void startRegulationControlLoop() throws Exception {
		((PerformanceRegulatorI)(this.connector)).startRegulationControlLoop();
	}

	

}
