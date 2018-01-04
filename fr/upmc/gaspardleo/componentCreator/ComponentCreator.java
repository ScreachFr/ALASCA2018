package fr.upmc.gaspardleo.componentCreator;

import java.lang.reflect.Constructor;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentStartException;

public class ComponentCreator 
	extends AbstractComponent {
	
	AbstractCVM cvm;
	
	
	public ComponentCreator(AbstractCVM cvm) throws ComponentStartException{
		super(1,0);
		this.cvm = cvm;
		this.cvm.addDeployedComponent(this);
	}
		
	public void createComponent(
			Class<?> clas,
			Object[] constructorParams
			) throws Exception{
		
		assert clas != null;
		assert constructorParams != null;
		
		if(AbstractCVM.isDistributed){
						
			this.distributedComponetCreation(clas, constructorParams);
			
		} else {
			
			this.componentCreation(clas, constructorParams);
		}
	}
	
	public void componentCreation(Class<?> clas,
			Object[] constructorParams
			) throws Exception{
		
//		System.out.println("[DEBUG LEO] clas : " + clas);
		
		Class<?>[] parameterTypes = new Class[constructorParams.length] ;
		for(int i = 0 ; i < constructorParams.length ; i++) {
			
//			System.out.println("[DEBUG LEO] constructorParams : " + constructorParams[i].getClass()); 
			
			parameterTypes[i] = constructorParams[i].getClass() ;
		}
		
		Constructor<?> cons = null;
		cons = clas.getConstructor(parameterTypes);
		
		assert cons != null : "assertion : cons null";
//		System.out.println("[DEBUG LEO] cons : " + cons);
		
		AbstractComponent component = 
				(AbstractComponent) cons.newInstance(constructorParams);
		
		component.start();
		
		this.cvm.addDeployedComponent(component);
	}
	
	public void distributedComponetCreation(Class<?> clas,
			Object[] constructorParams
			) throws Exception{
		
		if(!this.isRequiredInterface(DynamicComponentCreationI.class)){
			this.addRequiredInterface(DynamicComponentCreationI.class);
		}
		
		DynamicComponentCreationOutboundPort dccop = new DynamicComponentCreationOutboundPort(this);
		dccop.localPublishPort();;
		this.addPort(dccop);
		
		assert dccop != null;
		
		dccop.doConnection(
				AbstractDistributedCVM.thisJVMURI +
				AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX, 
				DynamicComponentCreationConnector.class.getCanonicalName());
		
		dccop.createComponent(clas.getCanonicalName(), constructorParams);
		
		dccop.doDisconnection();
		dccop.destroyPort();
	}	
}
