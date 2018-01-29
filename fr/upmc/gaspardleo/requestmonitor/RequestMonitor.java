package fr.upmc.gaspardleo.requestmonitor;


import java.util.HashMap;

import fr.upmc.components.AbstractComponent;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;
import fr.upmc.gaspardleo.requestmonitor.ports.RequestMonitorInboundPort;

public class RequestMonitor extends AbstractComponent implements RequestMonitorI {
	public final static Long RELEVANCE_WINDOW = 5000L; // 5 sec.
	
	private Object lock;
	
	
	public enum RequestMonitorPorts {
		INTROSPECTION,
		REQUEST_MONITOR_IN
	}
	
	private Double meanRequestExecutionTime;
	private Double alpha;
	private Boolean isFirstValue;
	
	private Long lastEntry;

	private RequestMonitorInboundPort rmip;
	
	public RequestMonitor(
		  	HashMap<RequestMonitorPorts, String> component_uris,
			Double alpha) throws Exception {
		
		super(1, 1);
		
		if (alpha < 0.0 || alpha > 1.0)
			throw new Error("RequestMonitor constructor : Wrong alpha value. This value must be between 0 and 1. It's current value is " + alpha + ".");
		
		this.lock = new Object();
		this.meanRequestExecutionTime = 0.0;
		this.lastEntry = -1L;
		this.alpha = alpha;
		this.isFirstValue = true;

		this.addOfferedInterface(RequestMonitorI.class);
		this.rmip = new RequestMonitorInboundPort(component_uris.get(RequestMonitorPorts.REQUEST_MONITOR_IN), this);
		this.addPort(this.rmip);
		this.rmip.publishPort();
		
		this.toggleLogging();
		this.toggleTracing();		
		this.logMessage("RequestMonitor made");
	}

	@Override
	public void addEntry(Long submissionTimestamp, Long notificationTimestamp) {
		
		long executionTime = notificationTimestamp - submissionTimestamp;

		refreshMeanRequestExecutionTime(executionTime);
	}
	
	@Override
	public Boolean isDataRelevant() {
		return !isFirstValue && (System.currentTimeMillis() - lastEntry) < RELEVANCE_WINDOW;
	}

	public void addEntry(Long executionTime) {
		refreshMeanRequestExecutionTime(executionTime);
	}

	private void refreshMeanRequestExecutionTime(Long executionTime) {
		synchronized (lock) {
			if (isFirstValue) {
				meanRequestExecutionTime = executionTime * 1.0;
				isFirstValue = false;
			} else
				meanRequestExecutionTime = (alpha * executionTime + ((1.0-alpha)) * meanRequestExecutionTime);
			
			lastEntry = System.currentTimeMillis();
		}
	}


	@Override
	public Double getMeanRequestExecutionTime() {
		synchronized (lock) {
			return meanRequestExecutionTime;
		}
	}
	
	public static HashMap<RequestMonitorPorts, String> makeUris(String rg_monitor_in, String rd_URI){
		HashMap<RequestMonitorPorts, String> rm_uris = new HashMap<>();
		rm_uris.put(RequestMonitorPorts.REQUEST_MONITOR_IN, rg_monitor_in);		
		rm_uris.put(RequestMonitorPorts.INTROSPECTION, "rm-" + rd_URI);
		return rm_uris;
	}
}
