package fr.upmc.gaspardleo.requestmonitor.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;

public class RequestMonitorConnector 
		extends AbstractConnector
		implements RequestMonitorI{

	@Override
	public void addEntry(Long submissionTimestamp, Long notificationTimestamp) throws Exception {
		((RequestMonitorI)this.offering).addEntry(submissionTimestamp, notificationTimestamp);
	}

	@Override
	public Double getMeanRequestExecutionTime() throws Exception {
		return ((RequestMonitorI)this.offering).getMeanRequestExecutionTime();
	}

	@Override
	public Boolean isDataRelevant() throws Exception {
		return ((RequestMonitorI)this.offering).isDataRelevant();
	}
}
