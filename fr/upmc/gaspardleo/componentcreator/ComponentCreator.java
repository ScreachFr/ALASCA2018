package fr.upmc.gaspardleo.componentcreator;

import java.lang.reflect.Constructor;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;

/**
 * La classe <code> ComponentCreator </ code>  implément un composant permettant 
 * la création dynamique d'autres composants
 * @author Leonor & Alexandre
 */
public class ComponentCreator 
	extends AbstractComponent {
	
	/** CVM pour le déploiment des composant */
	AbstractCVM cvm;
		
	/**
	 * @param cvm		CVM pour le déploiment des composant
	 * @throws Exception
	 */
	public ComponentCreator(AbstractCVM cvm) throws Exception{

		super(1,1);
		
		this.cvm = cvm;
		this.cvm.addDeployedComponent(this);
		
		this.toggleTracing();
		
		this.logMessage("ComponentCreator made");
		
	}
	
	/**
	 * Crétaion d'un composant en fonctionde si la CVM est distribuée
	 * @param clas					Classe du composant à instancier
	 * @param constructorParams		Valeurs des parmètres du constructeur du composant à instancier
	 * @throws Exception
	 */
	public void createComponent(
			Class<?> clas,
			Object[] constructorParams) throws Exception{
		
		assert clas != null;
		assert constructorParams != null;
		
		if(AbstractCVM.isDistributed){
			this.distributedComponetCreation(clas, constructorParams);
		} else {
			this.componentCreation(clas, constructorParams);
		}
	}
	
	/**
	 * Création non dynamique d'un composant
	 * @param clas					Classe du composant à instancier
	 * @param constructorParams		Valeurs des parmètres du constructeur du composant à instancier
	 * @throws Exception
	 */
	public void componentCreation(Class<?> clas,
			Object[] constructorParams) throws Exception{
				
		Class<?>[] parameterTypes = new Class[constructorParams.length] ;
		for(int i = 0 ; i < constructorParams.length ; i++) {			
			parameterTypes[i] = constructorParams[i].getClass() ;
		}
		
		Constructor<?> cons = null;
		cons = clas.getConstructor(parameterTypes);
		
		assert cons != null : "assertion : cons null";
		
		AbstractComponent component = (AbstractComponent) cons.newInstance(constructorParams);
		
		component.start();
		this.cvm.addDeployedComponent(component);
	}
	
	/**
	 * Création dynamique d'un composant
	 * @param clas					Classe du composant à instancier
	 * @param constructorParams		Valeurs des parmètres du constructeur du composant à instancier
	 * @throws Exception
	 */
	public void distributedComponetCreation(Class<?> clas,
			Object[] constructorParams) throws Exception{
		
		if(!this.isRequiredInterface(DynamicComponentCreationI.class))
			this.addRequiredInterface(DynamicComponentCreationI.class);
		
		DynamicComponentCreationOutboundPort dccop = new DynamicComponentCreationOutboundPort(this);
		dccop.localPublishPort();;
		this.addPort(dccop);
		
		assert dccop != null;
		
		dccop.doConnection(
				AbstractDistributedCVM.thisJVMURI +	AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				ClassFactory.newConnector(DynamicComponentCreationI.class).getCanonicalName());
		
		try{
			dccop.createComponent(clas.getCanonicalName(), constructorParams);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}

		dccop.doDisconnection();
		dccop.destroyPort();
	}	
}
