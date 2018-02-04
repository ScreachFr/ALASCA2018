package fr.upmc.gaspardleo.computerpool.interfaces;

import java.util.HashMap;

/**
 * Cette interface repr�sente le centre nerveux d'un r�seau de <code>ComputerPool</code>. 
 * En effet il permet de tenir un registre de <code>ComputerPool</code> disponible dans le syst�me.
 * 
 * @author Leonor & Alexandre
 */
public interface ComputerPoolNetworkMasterI {
	
	/**
	 * Ajoute un <code>ComputerPool</code> au registre.
	 * @param computerPoolUri
	 * 		URI du <code>ComputerPool</code>.
	 * @param compterPoolInboundPortUri
	 * 		URI du port <code>ComputerPoolI</code> in.
	 * @throws Exception
	 */
	public void registerComputerPool(String computerPoolUri, String compterPoolInboundPortUri) throws Exception;
	
	/**
	 * Supprime une entr�e du registre de <code>ComputerPool</code>.
	 * @param computerPoolUri
	 * 		Entr�e � enlever.
	 * @throws Exception
	 */
	public void unregisterComputerPool(String computerPoolUri) throws Exception;
	
	/**
	 * Retourne une liste de <code>ComputerPool</code> disponible.
	 * @return
	 * 		Map de <code>ComputerPool</code> qui contient en clef l'URI du <code>ComputerPool</code> et en valeur l'URI de sont port <code>ComputerPoolI</code> in.
	 * @throws Exception
	 */
	public HashMap<String, String> getAvailableComputerPools() throws Exception;
	
}
