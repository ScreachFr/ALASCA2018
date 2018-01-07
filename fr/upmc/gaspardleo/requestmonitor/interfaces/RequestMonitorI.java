package fr.upmc.gaspardleo.requestmonitor.interfaces;

public interface RequestMonitorI {
	
	public void addEntry(Long submissionTimestamp, Long notificationTimestamp) throws Exception;
	
	public Double getMeanRequestExecutionTime() throws Exception;
	
	public Boolean isDataRelevant() throws Exception;
}
