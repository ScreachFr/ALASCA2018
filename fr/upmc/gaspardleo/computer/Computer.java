package fr.upmc.gaspardleo.computer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.ports.AbstractPort;

public class Computer extends fr.upmc.datacenter.hardware.computers.Computer {
	
	public static enum ComputerPortsTypes {
		INTROSEPTION, SERVICE_IN, STATIC_STATE_IN, DYNAMIC_STATE_IN;
	}
	
	private String computerURI;
	
	public Computer(
			String computerURI, 
			Set<Integer> possibleFrequencies, 
			Map<Integer, Integer> processingPower,
			int defaultFrequency, 
			int maxFrequencyGap, 
			int numberOfProcessors, 
			int numberOfCores) throws Exception {
		
		super(
			computerURI, 
			possibleFrequencies, 
			processingPower, 
			defaultFrequency, 
			maxFrequencyGap, 
			numberOfProcessors,
			numberOfCores, 
			AbstractPort.generatePortURI(), 
			AbstractPort.generatePortURI(),
			AbstractPort.generatePortURI());
		
		this.computerURI = computerURI;
	}
	
	public Map<ComputerPortsTypes, String> getComputerPortsURI() throws Exception {
		
		Map<ComputerPortsTypes, String> result = new HashMap<>();
		
		result.put(ComputerPortsTypes.INTROSEPTION, 
				this.computerURI);		
		result.put(ComputerPortsTypes.SERVICE_IN, 
				this.computerServicesInboundPort.getPortURI());
		result.put(ComputerPortsTypes.STATIC_STATE_IN, 
				this.computerStaticStateDataInboundPort.getPortURI());
		result.put(ComputerPortsTypes.DYNAMIC_STATE_IN, 
				this.computerDynamicStateDataInboundPort.getPortURI());
		
		return result;
		
	}
}
