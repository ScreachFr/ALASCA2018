package fr.upmc.gaspardleo.performanceregulator.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator;
import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

/**
 * La classe <code> PerformanceRegulatorInboundPort </ code> implémente le port entrant 
 * offrant l'interface <code> PerformanceRegulatorI </ code>.
 * @author Leonor & Alexandre
 */
public 	class 		PerformanceRegulatorInboundPort 
		extends 	AbstractInboundPort 
		implements 	PerformanceRegulatorI{

	private static final long serialVersionUID = -8603941140083696346L;

	/**
	 * @param 	uri			URI de l'inbound port
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public PerformanceRegulatorInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, PerformanceRegulatorI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI#increaseCPUFrequency()
	 */
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

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI#decreaseCPUFrequency()
	 */
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

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI#addAVMToRD()
	 */
	@Override
	public Boolean addAVMToRD() throws Exception {
		PerformanceRegulator pr = (PerformanceRegulator)owner;
		try{
			return pr.handleRequestSync(
					new ComponentI.ComponentService<Boolean>(){
						@Override
						public Boolean call() throws Exception {
							return pr.addAVMToRD();
						}
					});
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI#removeAVMFromRD()
	 */
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

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI#setRegulationStrategie(RegulationStrategyI)
	 */
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

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI#getRegulationStrategie()
	 */
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

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI#startRegulationControlLoop()
	 */
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
