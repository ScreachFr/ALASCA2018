package fr.upmc.gaspardleo.performanceregulator.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;

/**
 * La classe <code> PerformanceRegulatorOutboundPort </ code> implémente le port sortant 
 * offrant l'interface <code> PerformanceRegulatorI </ code>.
 * @author Leonor & Alexandre
 */
public class PerformanceRegulatorOutboundPort 
		extends AbstractOutboundPort 
		implements PerformanceRegulatorI {

	private static final long serialVersionUID = 1L;

	/**
	 * @param 	uri			URI de l'inbound port
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public PerformanceRegulatorOutboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, PerformanceRegulatorI.class, owner);
	}

	/**
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public PerformanceRegulatorOutboundPort(ComponentI owner)
			throws Exception {
		super(PerformanceRegulatorI.class, owner);
	}
	
	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#increaseCPUFrequency()
	 */
	@Override
	public Boolean increaseCPUFrequency() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).increaseCPUFrequency();
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#decreaseCPUFrequency()
	 */
	@Override
	public Boolean decreaseCPUFrequency() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).decreaseCPUFrequency();
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#addAVMToRD()
	 */
	@Override
	public Boolean addAVMToRD() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).addAVMToRD();
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#removeAVMFromRD()
	 */
	@Override
	public Boolean removeAVMFromRD() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).removeAVMFromRD();
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#setRegulationStrategie(RegulationStrategyI)
	 */
	@Override
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception {
		((PerformanceRegulatorI)(this.connector)).setRegulationStrategie(strat);
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#getRegulationStrategie()
	 */
	@Override
	public RegulationStrategyI getRegulationStrategie() throws Exception {
		return ((PerformanceRegulatorI)(this.connector)).getRegulationStrategie();
	}

	/**
	 * @see fr.upmc.gaspardleo.performanceregulator.interfaces#startRegulationControlLoop()
	 */
	@Override
	public void startRegulationControlLoop() throws Exception {
		((PerformanceRegulatorI)(this.connector)).startRegulationControlLoop();
	}
}
