package fr.upmc.gaspardleo.computer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;

public class Computer extends fr.upmc.datacenter.hardware.computers.Computer {

	public static enum ComputerPortsTypes {
		INTROSPECTION,
		SERVICE_IN,
		STATIC_STATE_IN,
		DYNAMIC_STATE_IN;
	}
	
	private static final int CPU_FREQUENCY = 3000;
	private static final int CPU_MAX_FREQUENCY_GAP = 1500;
	private static final int NB_CPU = 2;
	private static final int NB_CORES = 4;
	
	public Computer(
			String computerURI,
			HashMap<ComputerPortsTypes, String> computer_uris,
			HashMap<ComputerPoolPorts, String> cp_uris,
			HashSet<Integer> admissibleFrequencies, 
			HashMap<Integer, Integer> processingPower,
			String computerServicesInboundPortURI,
			String computerStaticStateDataInboundPortURI,
			String computerDynamicStateDataInboundPortURI) throws Exception {
		
		super(
			computerURI,
			admissibleFrequencies,
			processingPower,
			CPU_FREQUENCY,
			CPU_MAX_FREQUENCY_GAP,
			NB_CPU,
			NB_CORES,
			computerServicesInboundPortURI,
			computerStaticStateDataInboundPortURI,
			computerDynamicStateDataInboundPortURI);	
		
		this.addRequiredInterface(ComputerPoolI.class);
		ComputerPoolOutboundPort cpop = new ComputerPoolOutboundPort(this);
		cpop.publishPort();
		this.addPort(cpop);
		
		cpop.doConnection(
				cp_uris.get(ComputerPoolPorts.COMPUTER_POOL),
				ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());
		
		cpop.addComputer(computer_uris, numberOfProcessors, numberOfCores);
	}


	public static Map<ComputerPortsTypes, String> newInstance(
			String computerURI,
			HashMap<ComputerPoolPorts, String> cp_uris,
			ComponentCreator cc) throws Exception {

		String computerServicesInboundPortURI = AbstractPort.generatePortURI();
		String computerStaticStateDataInboundPortURI = AbstractPort.generatePortURI();
		String computerDynamicStateDataInboundPortURI = AbstractPort.generatePortURI();
		
		HashMap<ComputerPortsTypes, String> computer_uris = new HashMap<>();
		computer_uris.put(ComputerPortsTypes.INTROSPECTION, computerURI);
		computer_uris.put(ComputerPortsTypes.SERVICE_IN, computerServicesInboundPortURI);
		computer_uris.put(ComputerPortsTypes.STATIC_STATE_IN, computerStaticStateDataInboundPortURI);
		computer_uris.put(ComputerPortsTypes.DYNAMIC_STATE_IN, computerDynamicStateDataInboundPortURI);

		HashSet<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500);
		admissibleFrequencies.add(3000);
		
		HashMap<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000);
		processingPower.put(3000, 3000000);

		Object[] constructorParams = new Object[] {
				computerURI,
				computer_uris,
				cp_uris,
				admissibleFrequencies,
				processingPower,
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

		return computer_uris;
	}
}
