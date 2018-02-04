package fr.upmc.gaspardleo.requestmonitor.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;

/**
 * La classe <code> RequestMonitorOutboundPort </ code> implémente le port sortrant 
 * offrant l'interface <code> RequestMonitorI </ code>.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		RequestMonitorOutboundPort 
		extends 	AbstractOutboundPort 
		implements 	RequestMonitorI {

	private static final long serialVersionUID = 1L;

	/**
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public RequestMonitorOutboundPort(ComponentI owner) throws Exception {
		super(RequestMonitorI.class, owner);
	}
	
	/**
	 * @see fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI#addEntry(Long, Long)
	 */
	@Override
	public void addEntry(Long submissionTimestamp, Long notificationTimestamp) throws Exception {
		((RequestMonitorI)(this.connector)).addEntry(submissionTimestamp, notificationTimestamp);
	}

	/**
	 * @see fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI#getMeanRequestExecutionTime()
	 */
	@Override
	public Double getMeanRequestExecutionTime() throws Exception {
		return ((RequestMonitorI)(this.connector)).getMeanRequestExecutionTime();
	}

	/**
	 * @see fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI#isDataRelevant()
	 */
	@Override
	public Boolean isDataRelevant() throws Exception {
		return ((RequestMonitorI)(this.connector)).isDataRelevant();
	}

}
