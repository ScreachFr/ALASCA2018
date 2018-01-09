package fr.upmc.gaspardleo.computerpool.interfaces;

import java.util.HashMap;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;

public interface ComputerPoolI extends OfferedI, RequiredI {

	/**
	 * Créé une nouvelle ApplicationVM.
	 * @param avmURI 
	 * 		URI de la nouvelle ApplicationVM.
	 * @param numberOfCoreToAllocate
	 * 		Nombre de coeur à allouer à l'ApplicationVM.
	 * @return
	 * 		La nouvelle ApplicationVM.
	 */
	public HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(String avmURI, 
			Integer numberOfCoreToAllocate,
			ComponentCreator cc) throws Exception;
	
	/**
	 * Ajoute un ordinateur au pool actuel du composant.
	 * Détails des paramètres disponibles à @see {@link fr.upmc.gaspardleo.computer.Computer}.
	 * @return
	 * 		Nouveau Computer.
	 */
	public void addComputer(
			HashMap<ComputerPortsTypes, String> computerUris,
			Integer numberOfProcessors,
			Integer numberOfCores) throws Exception;
}
