package fr.upmc.gaspardleo.computer;

import fr.upmc.components.ports.AbstractPort;

public class ComputerMonitor 
	extends fr.upmc.datacenter.hardware.tests.ComputerMonitor {

	public enum ComputerMonitorPortTypes {
		STATIC_STATE_OUT, DYNAMIC_STATE_OUT;
	}
	
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
