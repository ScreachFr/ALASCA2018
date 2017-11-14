package fr.upmc.gaspardleo.computer;

import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.ports.AbstractPort;

public class ComputerMonitor extends fr.upmc.datacenter.hardware.tests.ComputerMonitor {

	public enum ComputerMonitorPortTypes {
		STATIC_STATE_OUT, DYNAMIC_STATE_OUT;
	}
	
	public ComputerMonitor(
			String computerURI, 
			boolean active) throws Exception {
		
		super(
			computerURI, 
			active, 
			AbstractPort.generatePortURI(), 
			AbstractPort.generatePortURI());
	}
	
	public Map<ComputerMonitorPortTypes, String> getPortTypes() throws Exception {
		
		Map<ComputerMonitorPortTypes, String> result = new HashMap<>();
		
		result.put(ComputerMonitorPortTypes.STATIC_STATE_OUT, 
				this.cssPort.getPortURI());
		result.put(ComputerMonitorPortTypes.DYNAMIC_STATE_OUT, 
				this.cdsPort.getPortURI());
		
		return result;
	}

}
