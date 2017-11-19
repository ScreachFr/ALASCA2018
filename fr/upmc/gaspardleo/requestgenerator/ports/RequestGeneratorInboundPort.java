package fr.upmc.gaspardleo.requestgenerator.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorI;

public class RequestGeneratorInboundPort
	extends AbstractInboundPort
	implements RequestGeneratorI{

	private static final long serialVersionUID = 1L;

	public RequestGeneratorInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RequestGeneratorI.class, owner);
	}
}
