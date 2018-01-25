package fr.upmc.gaspardleo.requestmonitor.interfaces;

public interface RequestMonitorI {
	
	/**
	 * Ajoute une entrée au moniteur.
	 * @param submissionTimestamp
	 * 		Heure d'arrivée de l'entrée.
	 * @param queueExitTimestamp
	 * 		Heure de sortie de l'entrée de la file d'attente.
	 * @throws Exception
	 */
	public void addEntry(Long submissionTimestamp, Long queueExitTimestamp) throws Exception;
	
	/**
	 * Donne la durée moyenne d'attente avant le traitement d'une requête.
	 * @return
	 * 		Durée moyenne.
	 * @throws Exception
	 */
	public Double getMeanRequestExecutionTime() throws Exception;
	
	/**
	 * Indique si les mesures sont encore pertinentes. Cela peut permettre d'indiquer des données périmées.
	 * @return
	 * 	Les données sont-elle pertinentes ?
	 * @throws Exception
	 */
	public Boolean isDataRelevant() throws Exception;
}
