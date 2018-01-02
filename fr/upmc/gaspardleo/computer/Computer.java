package fr.upmc.gaspardleo.computer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;

public class Computer extends fr.upmc.datacenter.hardware.computers.Computer {

	public static enum ComputerPortsTypes {
		INTROSPECTION,
		SERVICE_IN,
		STATIC_STATE_IN,
		DYNAMIC_STATE_IN;
	}
	
	public Computer(
			String computerURI,
			HashSet<Integer> possibleFrequencies, 
			HashMap<Integer, Integer> processingPower,
			Integer defaultFrequency, 
			Integer maxFrequencyGap, 
			Integer numberOfProcessors, 
			Integer numberOfCores,
			String computerServicesInboundPortURI,
			String computerStaticStateDataInboundPortURI,
			String computerDynamicStateDataInboundPortURI) throws Exception {
		
		super(
			computerURI,
			possibleFrequencies,
			processingPower,
			defaultFrequency,
			maxFrequencyGap,
			numberOfProcessors,
			numberOfCores,
			computerServicesInboundPortURI,
			computerStaticStateDataInboundPortURI,
			computerDynamicStateDataInboundPortURI);		
	}


	public static Map<ComputerPortsTypes, String> newInstance(
			String computerURI,
			HashSet<Integer> possibleFrequencies,
			HashMap<Integer, Integer> processingPower,
			Integer defaultFrequency,
			Integer maxFrequencyGap,
			Integer numberOfProcessors,
			Integer numberOfCores,
			ComponentCreator cc) throws Exception {

		String computerServicesInboundPortURI = AbstractPort.generatePortURI();
		String computerStaticStateDataInboundPortURI = AbstractPort.generatePortURI();
		String computerDynamicStateDataInboundPortURI = AbstractPort.generatePortURI();

		Object[] constructorParams = new Object[] {
				computerURI,
				possibleFrequencies,
				processingPower,
				defaultFrequency, 
				maxFrequencyGap, 
				numberOfProcessors, 
				numberOfCores,
				computerServicesInboundPortURI, 
				computerStaticStateDataInboundPortURI,
				computerDynamicStateDataInboundPortURI,
		};

		System.out.println("Computer factory call");
		
		try {
			cc.createComponent(Computer.class, constructorParams);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		System.out.println("Computer factory done");

		Map<ComputerPortsTypes, String> result = new HashMap<>();
		result.put(ComputerPortsTypes.INTROSPECTION, computerURI);
		result.put(ComputerPortsTypes.SERVICE_IN, computerServicesInboundPortURI);
		result.put(ComputerPortsTypes.STATIC_STATE_IN, computerStaticStateDataInboundPortURI);
		result.put(ComputerPortsTypes.DYNAMIC_STATE_IN, computerDynamicStateDataInboundPortURI);

		return result;
	}
}
