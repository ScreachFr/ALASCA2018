package fr.upmc.gaspardleo.requestmonitor.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;

public class RequestMonitorOutboundPort extends AbstractOutboundPort implements RequestMonitorI{

	public RequestMonitorOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RequestMonitorI.class, owner);
	}

	@Override
	public void addEntry(Long submissionTimestamp, Long notificationTimestamp) throws Exception {
		((RequestMonitorI)(this.connector)).addEntry(submissionTimestamp, notificationTimestamp);
	}

	@Override
	public Double getMeanRequestExecutionTime() throws Exception {
		return ((RequestMonitorI)(this.connector)).getMeanRequestExecutionTime();
	}

}
