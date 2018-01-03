package fr.upmc.gaspardleo.componentCreator;

import java.lang.reflect.Constructor;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;

public class ComponentCreator 
	extends AbstractComponent {
	
	public ComponentCreator() throws Exception{
		super(1, 0);
	}
		
	public void createComponent(
			Class<?> clas,
			Object[] constructorParams
			) throws Exception{
		
		assert clas != null;
		assert constructorParams != null;
		
		if(!AbstractCVM.isDistributed){
			
			this.componentCreation(clas, constructorParams);
			
		} else {
			
			this.dynamicComponetCreation(clas, constructorParams);
		}
	}
	
	private void componentCreation(Class<?> clas,
			Object[] constructorParams
			) throws Exception{
		
		Class<?>[] parameterTypes = new Class[constructorParams.length] ;
		for(int i = 0 ; i < constructorParams.length ; i++) {
			parameterTypes[i] = constructorParams[i].getClass() ;
		}
		
		Constructor<?> cons = null;
		cons = clas.getConstructor(parameterTypes);
		
		AbstractComponent component = 
				(AbstractComponent) cons.newInstance(constructorParams);
		
		component.start();
	}
	
	private void dynamicComponetCreation(Class<?> clas,
			Object[] constructorParams
			) throws Exception{
		
		if(!this.isRequiredInterface(DynamicComponentCreationI.class)){
			this.addRequiredInterface(DynamicComponentCreationI.class);
		}
		
		DynamicComponentCreationOutboundPort dccop = new DynamicComponentCreationOutboundPort(this);
		dccop.localPublishPort();
		this.addPort(dccop);
		
		assert dccop != null;
		
		dccop.doConnection(
				AbstractDistributedCVM.thisJVMURI +
				AbstractDistributedCVM.DCC_INBOUNDPORT_URI_SUFFIX, 
				DynamicComponentCreationConnector.class.getCanonicalName());
		
		dccop.createComponent(clas.getCanonicalName(), constructorParams);
		
		dccop.doDisconnection();
		dccop.destroyPort();
	}
}
