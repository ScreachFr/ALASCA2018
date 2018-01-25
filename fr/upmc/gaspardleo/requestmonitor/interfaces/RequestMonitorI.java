package fr.upmc.gaspardleo.requestmonitor.interfaces;

public interface RequestMonitorI {
	
	/**
	 * Ajoute une entr�e au moniteur.
	 * @param submissionTimestamp
	 * 		Heure d'arriv�e de l'entr�e.
	 * @param queueExitTimestamp
	 * 		Heure de sortie de l'entr�e de la file d'attente.
	 * @throws Exception
	 */
	public void addEntry(Long submissionTimestamp, Long queueExitTimestamp) throws Exception;
	
	/**
	 * Donne la dur�e moyenne d'attente avant le traitement d'une requ�te.
	 * @return
	 * 		Dur�e moyenne.
	 * @throws Exception
	 */
	public Double getMeanRequestExecutionTime() throws Exception;
	
	/**
	 * Indique si les mesures sont encore pertinentes. Cela peut permettre d'indiquer des donn�es p�rim�es.
	 * @return
	 * 	Les donn�es sont-elle pertinentes ?
	 * @throws Exception
	 */
	public Boolean isDataRelevant() throws Exception;
}
