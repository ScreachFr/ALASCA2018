package fr.upmc.gaspardleo.requestmonitor;


import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;
import fr.upmc.gaspardleo.requestmonitor.ports.RequestMonitorInboundPort;

public class RequestMonitor extends AbstractComponent implements RequestMonitorI {
	
	public enum RequestMonitorPorts {
		REQUEST_MONITOR_IN;
	}
	
	private Double meanRequestExecutionTime;
	private Double alpha;
	private Boolean isFirstValue;

	private RequestMonitorInboundPort rmip;
	
	public RequestMonitor(String componentUri, String requestMonitorIn_URI, Double alpha) throws Exception {
		super(1, 1);
		
		if (alpha < 0 || alpha > 0)
			throw new Error("RequestMonitor constructor : Wrong alpha value. This value must be between 0 and 1. It's current value is " + alpha + ".");
		
		this.meanRequestExecutionTime = 0.0;
		this.alpha = alpha;
		this.isFirstValue = true;
		
		
		this.addOfferedInterface(RequestMonitorI.class);
		this.rmip = new RequestMonitorInboundPort(requestMonitorIn_URI, this);
		this.addPort(this.rmip);
		this.rmip.publishPort();
		
	}


	@Override
	public void addEntry(Long submissionTimestamp, Long notificationTimestamp) {
		long executionTime = notificationTimestamp - submissionTimestamp;

		refreshMeanRequestExecutionTime(executionTime);
	}

	public void addEntry(Long executionTime) {
		refreshMeanRequestExecutionTime(executionTime);
	}

	private void refreshMeanRequestExecutionTime(Long executionTime) {
		if (isFirstValue) {
			meanRequestExecutionTime = executionTime * 1.0;
			isFirstValue = false;
		} else
			meanRequestExecutionTime = (alpha * executionTime + ((1.0-alpha)) * meanRequestExecutionTime);
	}


	@Override
	public Double getMeanRequestExecutionTime() {
		return meanRequestExecutionTime;
	}
	
	
	public static Map<RequestMonitorPorts, String> newInstance(
			DynamicComponentCreationOutboundPort dcc, 
			String componentUri,
			Double alpha) throws Exception {
		
		String requestMonitorIn_URI = AbstractPort.generatePortURI();
		
		
		Object[] args = new Object[] {
				componentUri,
				requestMonitorIn_URI,
				alpha
		};
		
		dcc.createComponent(RequestMonitor.class.getCanonicalName(),
				args);
		
		
		Map<RequestMonitorPorts, String> result = new HashMap<>();
		
		result.put(RequestMonitorPorts.REQUEST_MONITOR_IN, requestMonitorIn_URI);		
		
		return result;
	}


}
