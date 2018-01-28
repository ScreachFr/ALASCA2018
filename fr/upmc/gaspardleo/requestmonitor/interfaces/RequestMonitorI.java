package fr.upmc.gaspardleo.requestmonitor.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface RequestMonitorI 
		extends	OfferedI, RequiredI{
	
	public void addEntry(Long submissionTimestamp, Long notificationTimestamp) throws Exception;
	
	public Double getMeanRequestExecutionTime() throws Exception;
	
	public Boolean isDataRelevant() throws Exception;
}
