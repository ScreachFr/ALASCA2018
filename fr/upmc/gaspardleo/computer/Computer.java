package fr.upmc.gaspardleo.computer;

import java.util.HashMap;
import java.util.HashSet;

import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;

public class Computer extends fr.upmc.datacenter.hardware.computers.Computer {

	public static enum ComputerPortsTypes {
		INTROSPECTION,
		SERVICE_IN,
		STATIC_STATE_IN,
		DYNAMIC_STATE_IN
	}
	
	private static final int CPU_FREQUENCY = 3000;
	private static final int CPU_MAX_FREQUENCY_GAP = 1500;
	private static final int NB_CPU = 2;
	private static final int NB_CORES = 4;
	
	public Computer(
			HashMap<ComputerPortsTypes, String> computer_uris,
			HashMap<ComputerPoolPorts, String> cp_uris,
			HashSet<Integer> admissibleFrequencies, 
			HashMap<Integer, Integer> processingPower
			) throws Exception {
		
		super(
			computer_uris.get(ComputerPortsTypes.INTROSPECTION),
			admissibleFrequencies,
			processingPower,
			CPU_FREQUENCY,
			CPU_MAX_FREQUENCY_GAP,
			NB_CPU,
			NB_CORES,
			computer_uris.get(ComputerPortsTypes.SERVICE_IN),
			computer_uris.get(ComputerPortsTypes.STATIC_STATE_IN),
			computer_uris.get(ComputerPortsTypes.DYNAMIC_STATE_IN));	
		
		this.addRequiredInterface(ComputerPoolI.class);
		ComputerPoolOutboundPort cpop = new ComputerPoolOutboundPort(this);
		cpop.publishPort();
		this.addPort(cpop);

		cpop.doConnection(
			cp_uris.get(ComputerPoolPorts.COMPUTER_POOL),
			ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());

		cpop.addComputer(computer_uris, numberOfProcessors, numberOfCores);
		
		this.toggleTracing();
		this.logMessage("Computer made");
	}

	public static HashSet<Integer> makeFrequencies(){
		HashSet<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500);
		admissibleFrequencies.add(3000);
		return admissibleFrequencies;
	}
	
	public static HashMap<Integer,Integer> makeProcessingPower(){
		HashMap<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000);
		processingPower.put(3000, 3000000);
		return processingPower;
	}
	
	public static HashMap<ComputerPortsTypes, String> makeUris(int num_computer){
		HashMap<ComputerPortsTypes, String> computer_uris = new HashMap<>();
		computer_uris.put(ComputerPortsTypes.INTROSPECTION, "computer-"+num_computer);
		computer_uris.put(ComputerPortsTypes.SERVICE_IN, AbstractPort.generatePortURI());
		computer_uris.put(ComputerPortsTypes.STATIC_STATE_IN, AbstractPort.generatePortURI());
		computer_uris.put(ComputerPortsTypes.DYNAMIC_STATE_IN, AbstractPort.generatePortURI());
		return computer_uris;
	}
}
