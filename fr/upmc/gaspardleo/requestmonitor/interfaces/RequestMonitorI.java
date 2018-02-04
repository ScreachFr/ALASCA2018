package fr.upmc.gaspardleo.requestmonitor.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * L'interface <code> RequestMonitorI </ code> définit le comportement de l'objet RequestMonitor
 * pour le contrôlle des requêtes.
 * 
 * @author Leonor & Alexandre
 */
public 	interface 	RequestMonitorI 
		extends		OfferedI, 
					RequiredI {
	
	/**
	 * Ajoute une entrée au moniteur.
	 * @param 	submissionTimestamp 	Heure d'arrivée de l'entrée.
	 * @param 	queueExitTimestamp 		Heure de sortie de l'entrée de la file d'attente.
	 * @throws 	Exception
	 */
	public void addEntry(Long submissionTimestamp, Long queueExitTimestamp) throws Exception;
	
	/**
	 * Donne la durée moyenne d'attente avant le traitement d'une requête.
	 * @return Durée moyenne.
	 * @throws Exception
	 */
	public Double getMeanRequestExecutionTime() throws Exception;
	
	/**
	 * Indique si les mesures sont encore pertinentes. Cela peut permettre d'indiquer que des données sont périmées.
	 * @return Les données sont-elle pertinentes ?
	 * @throws Exception
	 */
	public Boolean isDataRelevant() throws Exception;
}
