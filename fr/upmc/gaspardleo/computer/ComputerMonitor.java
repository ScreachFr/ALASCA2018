package fr.upmc.gaspardleo.computer;

import fr.upmc.components.ports.AbstractPort;

/**
 * La classe <code> ComputerMonitor </ code> implémente le composant représentant 
 * le contrôlleur d'un ordinateur dans le centre de données.
 * @author Leonor & Alexandre
 */
public class ComputerMonitor 
	extends fr.upmc.datacenter.hardware.tests.ComputerMonitor {

	public enum ComputerMonitorPortTypes {
		STATIC_STATE_OUT, DYNAMIC_STATE_OUT;
	}
	
	/**
	 * @param computerURI	URI de l'ordinateur à contrôller
	 * @param active		État d'activité
	 * @throws Exception
	 */
	public ComputerMonitor(
		String computerURI, 
		boolean active
		) throws Exception {
		
		super(
			computerURI, 
			active, 
			AbstractPort.generatePortURI(), 
			AbstractPort.generatePortURI());
		
		this.toggleTracing();
		this.logMessage("ComputerMonitor made");
	}
}
