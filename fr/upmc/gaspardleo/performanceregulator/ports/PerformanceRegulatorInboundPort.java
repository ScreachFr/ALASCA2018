package fr.upmc.gaspardleo.performanceregulator.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator;
import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

public class PerformanceRegulatorInboundPort 
		extends AbstractInboundPort 
		implements PerformanceRegulatorI{

	private static final long serialVersionUID = -8603941140083696346L;

	public PerformanceRegulatorInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, PerformanceRegulatorI.class, owner);
	}


	@Override
	public Boolean increaseCPUFrequency() throws Exception {
		PerformanceRegulator pr = (PerformanceRegulator)owner;

		return pr.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return pr.increaseCPUFrequency();
					}
				});
	}

	@Override
	public Boolean decreaseCPUFrequency() throws Exception {
		PerformanceRegulator pr = (PerformanceRegulator)owner;

		return pr.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return pr.decreaseCPUFrequency();
					}
				});
	}

	@Override
	public Boolean addAVMToRD() throws Exception {
		PerformanceRegulator pr = (PerformanceRegulator)owner;

		return pr.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return pr.addAVMToRD();
					}
				});
	}

	@Override
	public Boolean removeAVMFromRD() throws Exception {
		PerformanceRegulator pr = (PerformanceRegulator)owner;

		return pr.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return pr.removeAVMFromRD();
					}
				});
	}

	@Override
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception {
		PerformanceRegulator pr = (PerformanceRegulator)owner;

		pr.handleRequestAsync(
				new ComponentI.ComponentService<PerformanceRegulator>(){
					@Override
					public PerformanceRegulator call() throws Exception {
						pr.setRegulationStrategie(strat);

						return pr;
					}
				});
	}

	@Override
	public RegulationStrategyI getRegulationStrategie() throws Exception {
		PerformanceRegulator pr = (PerformanceRegulator)owner;

		return pr.handleRequestSync(
				new ComponentI.ComponentService<RegulationStrategyI>(){
					@Override
					public RegulationStrategyI call() throws Exception {
						return pr.getRegulationStrategie();
					}
				});
	}

	@Override
	public void startRegulationControlLoop() throws Exception {
		PerformanceRegulator pr = (PerformanceRegulator)owner;

		pr.handleRequestAsync(
				new ComponentI.ComponentService<PerformanceRegulator>(){
					@Override
					public PerformanceRegulator call() throws Exception {
						pr.startRegulationControlLoop();

						return pr;
					}
				});
	}

}
