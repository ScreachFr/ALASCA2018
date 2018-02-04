package fr.upmc.gaspardleo.requestmonitor;

import java.util.HashMap;

import fr.upmc.components.AbstractComponent;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;
import fr.upmc.gaspardleo.requestmonitor.ports.RequestMonitorInboundPort;

/**
 * La classe <code> RequestMonitor </ code> implémente le composant représentant 
 * le contrôller de requêts dans le centre de calcul.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		RequestMonitor 
		extends 	AbstractComponent 
		implements 	RequestMonitorI {
	
	public enum RequestMonitorPorts {
		INTROSPECTION,
		REQUEST_MONITOR_IN
	}
	
	/** Fenêtre de pertinance */
	public final static Long RELEVANCE_WINDOW = 5000L; // 5 sec.
	/** Véroux pour la synchronisation */
	private Object lock;
	/** Temps moyen d'exécution de la requête */
	private Double meanRequestExecutionTime;
	/** Coefficient pour le calcul du temps moyen d'exécution de la requête */
	private Double alpha;
	/** Booléen pour le calcul de la pertinance de la donnée */
	private Boolean isFirstValue;
	/** Heure d'arrivéd de la dernière entrée de données */
	private Long lastEntry;
	/** Inboud port offrant les services du RequestMonitot */
	private RequestMonitorInboundPort rmip;
	
	/**
	 * @param 	component_uris	URIs du composant.
	 * @param 	alpha			Coefficient pour le calcul du temps moyen d'exécution de la requête.
	 * @throws 	Exception
	 */
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

	/**
	 * @see fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI#addEntry(Long, Long)
	 */
	@Override
	public void addEntry(Long submissionTimestamp, Long notificationTimestamp) {
		
		long executionTime = notificationTimestamp - submissionTimestamp;

		refreshMeanRequestExecutionTime(executionTime);
	}
	
	/**
	 * @see fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI#isDataRelevant()
	 */
	@Override
	public Boolean isDataRelevant() {
		return !isFirstValue && (System.currentTimeMillis() - lastEntry) < RELEVANCE_WINDOW;
	}

	/**
	 * Ajoute une entrée pour le calcul du temps moyen d'exécution d'une requête.
	 * @param executionTime Temps d'éxection de la dernière requête.
	 */
	public void addEntry(Long executionTime) {
		refreshMeanRequestExecutionTime(executionTime);
	}

	/**
	 * Met à jour le temps moyen d'éxection d'une requête avec du temps d'éxection de la dernière requête.
	 * @param executionTime		Temps d'éxection de la dernière requête.
	 */
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

	/**
	 * @see fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI#getMeanRequestExecutionTime()
	 */
	@Override
	public Double getMeanRequestExecutionTime() {
		synchronized (lock) {
			return meanRequestExecutionTime;
		}
	}
	
	/**
	 * Construit les URIs du composant et de ses ports.
	 * @param 	rg_monitor_in	URI de l'inboud port du composant.
	 * @param 	rd_URI			URI RequestDispatcher associé au RequestMonitor.
	 * @return					Les URIs du composant et de ses ports.
	 */
	public static HashMap<RequestMonitorPorts, String> makeUris(String rg_monitor_in, String rd_URI){
		HashMap<RequestMonitorPorts, String> rm_uris = new HashMap<>();
		rm_uris.put(RequestMonitorPorts.REQUEST_MONITOR_IN, rg_monitor_in);		
		rm_uris.put(RequestMonitorPorts.INTROSPECTION, "rm-" + rd_URI);
		return rm_uris;
	}
}
