package fr.upmc.gaspardleo.computer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreator;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.extensions.synchronizers.components.DistributedSynchronizerManager;
import fr.upmc.components.extensions.synchronizers.components.SynchronizerManager;
import fr.upmc.components.ports.AbstractPort;

public class Computer extends fr.upmc.datacenter.hardware.computers.Computer {

	private String computerURI;

	public Computer(String computerURI,
			HashSet<Integer> possibleFrequencies, HashMap<Integer, Integer> processingPower,
			Integer defaultFrequency, Integer maxFrequencyGap, Integer numberOfProcessors, Integer numberOfCores,
			String computerServicesInboundPortURI, String computerStaticStateDataInboundPortURI,
			String computerDynamicStateDataInboundPortURI
			) throws Exception {
		super(computerURI,
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


	public static Map<ComputerPortsTypes, String> newInstance(String computerURI,
			HashSet<Integer> possibleFrequencies,
			HashMap<Integer, Integer> processingPower,
			Integer defaultFrequency,
			Integer maxFrequencyGap,
			Integer numberOfProcessors,
			Integer numberOfCores,
			SynchronizerManager sm,
			Boolean distributed) throws Exception {



		String computerServicesInboundPortURI = AbstractPort.generatePortURI();
		String computerStaticStateDataInboundPortURI = AbstractPort.generatePortURI();
		String computerDynamicStateDataInboundPortURI = AbstractPort.generatePortURI();


		Object[] args = new Object[] {
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
			if (! distributed)
				sm.createComponent(Computer.class, args);
			else
				((DistributedSynchronizerManager)sm).createComponent(Computer.class, args);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		System.out.println("Computer factory done");

		Map<ComputerPortsTypes, String> result = new HashMap<>();

		result.put(ComputerPortsTypes.INTROSEPTION, 
				computerURI);		
		result.put(ComputerPortsTypes.SERVICE_IN, 
				computerServicesInboundPortURI);
		result.put(ComputerPortsTypes.STATIC_STATE_IN, 
				computerStaticStateDataInboundPortURI);
		result.put(ComputerPortsTypes.DYNAMIC_STATE_IN, 
				computerDynamicStateDataInboundPortURI);

		return result;
	}

	public static enum ComputerPortsTypes {
		INTROSEPTION, SERVICE_IN, STATIC_STATE_IN, DYNAMIC_STATE_IN;
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
