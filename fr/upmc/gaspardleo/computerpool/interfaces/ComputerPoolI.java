package fr.upmc.gaspardleo.computerpool.interfaces;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;

public interface ComputerPoolI extends OfferedI, RequiredI {
	
	
	/**
	 * Ajoute un ordinateur au pool actuel du composant.
	 * Détails des paramètres disponibles à @see {@link fr.upmc.gaspardleo.computer.Computer}.
	 * @return
	 * 		Nouveau Computer.
	 */
	public void createNewComputer(String computerURI,
			HashSet<Integer> possibleFrequencies,
			HashMap<Integer, Integer> processingPower,
			Integer defaultFrequency,
			Integer maxFrequencyGap,
			Integer numberOfProcessors,
			Integer numberOfCores) throws Exception;
	
	/**
	 * Créé une nouvelle ApplicationVM.
	 * @param avmURI 
	 * 		URI de la nouvelle ApplicationVM.
	 * @param numberOfCoreToAllocate
	 * 		Nombre de coeur à allouer à l'ApplicationVM.
	 * @return
	 * 		La nouvelle ApplicationVM.
	 */
	public Map<ApplicationVMPortTypes, String> createNewApplicationVM(String avmURI, Integer numberOfCoreToAllocate) throws Exception;
}
