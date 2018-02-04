package fr.upmc.gaspardleo.requestmonitor.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.requestmonitor.RequestMonitor;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;

/**
 * La classe <code> RequestMonitorInboundPort </ code> implémente le port entrant 
 * offrant l'interface <code> RequestMonitorI </ code>.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		RequestMonitorInboundPort 
		extends 	AbstractInboundPort 
		implements 	RequestMonitorI {

	private static final long serialVersionUID = -7495411833908280793L;

	/**
	 * @param 	uri			URI de l'inbound port.
	 * @param 	owner		Composant propriétaire du port.
	 * @throws 	Exception
	 */
	public RequestMonitorInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RequestMonitorI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI#addEntry(Long, Long)
	 */
	@Override
	public void addEntry(Long submissionTimestamp, Long notificationTimestamp) throws Exception {
		RequestMonitor owner = (RequestMonitor) this.owner;
		owner.handleRequestAsync(new ComponentService<RequestMonitorI>() {
			@Override
			public RequestMonitorI call() throws Exception {
				owner.addEntry(submissionTimestamp, notificationTimestamp);
				return owner;
			}
		});
	}

	/**
	 * @see fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI#getMeanRequestExecutionTime()
	 */
	@Override
	public Double getMeanRequestExecutionTime() throws Exception {
		RequestMonitor owner = (RequestMonitor) this.owner;
		return owner.handleRequestSync(new ComponentService<Double>() {
			@Override
			public Double call() throws Exception {
				return owner.getMeanRequestExecutionTime();
			}
		});
	}

	/**
	 * @see fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI#isDataRelevant()
	 */
	@Override
	public Boolean isDataRelevant() throws Exception {
		RequestMonitor owner = (RequestMonitor) this.owner;
		return owner.handleRequestSync(new ComponentService<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return owner.isDataRelevant();
			}
		});
	}
}
