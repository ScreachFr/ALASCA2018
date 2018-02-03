package fr.upmc.gaspardleo.computer;

import java.util.HashMap;
import java.util.HashSet;

import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;

/**
 * La classe <code> ApplicationVM </ code> implémente le composant représentant 
 * un ordinateur dans le centre de données.
 * @author Leonor & Alexandre
 */
public class Computer 
	extends fr.upmc.datacenter.hardware.computers.Computer {

	public static enum ComputerPortsTypes {
		INTROSPECTION,
		SERVICE_IN,
		STATIC_STATE_IN,
		DYNAMIC_STATE_IN
	}
	
	/** Fréquence de la CPU */
	private static final int CPU_FREQUENCY = 3000;
	/** Gap de la fréquence maximum de la CPU */
	private static final int CPU_MAX_FREQUENCY_GAP = 1500;
	/** Nombre de CPU */
	private static final int NB_CPU = 2;
	/** Nombre de coeurs */
	private static final int NB_CORES = 4;
	
	/**
	 * @param computer_uris				URIS du composant et de ses ports
	 * @param cp_uris					URI du ComputerPool auquel il est associé
	 * @param admissibleFrequencies		Fréquences admissibles
	 * @param processingPower			Puissance de calcul
	 * @throws Exception
	 */
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

	/**
	 * Crée les valeurs de fréquences admissibles
	 * @return	Les valeurs de fréquences admissibles
	 */
	public static HashSet<Integer> makeFrequencies(){
		HashSet<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500);
		admissibleFrequencies.add(3000);
		return admissibleFrequencies;
	}
	
	/**
	 * Crée les valeurs de puissance de calcul
	 * @return	Les valeurs de puissance de calcul
	 */
	public static HashMap<Integer,Integer> makeProcessingPower(){
		HashMap<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000);
		processingPower.put(3000, 3000000);
		return processingPower;
	}
	
	/**
	 * Crée les URIs du composant et de ses ports
	 * @param num_computer	URI du composant en lui même
	 * @return
	 */
	public static HashMap<ComputerPortsTypes, String> makeUris(int num_computer){
		HashMap<ComputerPortsTypes, String> computer_uris = new HashMap<>();
		computer_uris.put(ComputerPortsTypes.INTROSPECTION, "computer-"+num_computer);
		computer_uris.put(ComputerPortsTypes.SERVICE_IN, AbstractPort.generatePortURI());
		computer_uris.put(ComputerPortsTypes.STATIC_STATE_IN, AbstractPort.generatePortURI());
		computer_uris.put(ComputerPortsTypes.DYNAMIC_STATE_IN, AbstractPort.generatePortURI());
		return computer_uris;
	}
}
