package fr.upmc.gaspardleo.classfactory;

import fr.upmc.components.connectors.AbstractConnector;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * La classe <code> ClassFactory </ code> permet la création connector via Javassist
 * @author Leonor & Alexandre
 */
public class ClassFactory {
	
	/** compteur pour la construction des noms uniques des connecteurs */
	private static int id = 0;
	
	/**
	 * Crée un nouveau connecteur à partir de l'interface spécifiée en paramètre
	 * @param implementedInterface		Interface implémentée par le connecteur
	 * @return							Un connecteur
	 * @throws NotFoundException
	 * @throws CannotCompileException
	 */
	public static Class<?> newConnector(Class<?> implementedInterface) 
			throws NotFoundException, CannotCompileException {
		
		return newConnector(implementedInterface.getName() + "Connector" + (id++), 
				AbstractConnector.class, implementedInterface);
	}
	
	/**
	 * Crée un nouveau connecteur à partir d'un nom, de la classe qui l'étend et de l'interface qu'il implémente
	 * @param connectorName			Nom du connecteur
	 * @param extendedClass			Classes étendues par le connecteur soit AbstractConnector
	 * @param implementedInterface	Interface implémentée par le connecteur
	 * @return						Un connecteur
	 * @throws NotFoundException
	 * @throws CannotCompileException
	 */
	public static Class<?> newConnector(String connectorName, Class<?> extendedClass, Class<?> implementedInterface) throws NotFoundException, CannotCompileException {
		
		ClassPool cpool = ClassPool.getDefault();
		
		CtClass extendedClassCt = cpool.get(extendedClass.getCanonicalName());
		CtClass implementedInterfaceCt = cpool.get(implementedInterface.getCanonicalName());
		
		CtClass result = cpool.makeClass(connectorName);
		
		result.addInterface(implementedInterfaceCt);
		result.setSuperclass(extendedClassCt);
		
		for (CtMethod m : implementedInterfaceCt.getDeclaredMethods()) {
			//Signature
			String source = "public ";
			
			source += m.getReturnType().getName() + " ";
			source += m.getName() + "(";
			
			CtClass[] paramTypes = m.getParameterTypes();
			
			for (int i = 0; i < paramTypes.length; i++) {
				source += paramTypes[i].getName() + " p" + i;
				if (i < paramTypes.length-1)
					source += ", ";
			}
			
			source += ") {\n";
			
			// Body
			source += getConnectorMethodBody(implementedInterface, m.getName(), paramTypes.length);
			
			source += "\n}\n";
			
			result.addMethod(CtMethod.make(source, result));
		}
		
		return result.toClass();
	}
	
	/**
	 * Construit l'implémentation d'une méthode de l'interface qu'il implément grâce au nom de la méthode 
	 * en question est du nombre de parèmtres qu'elle utilise
	 * @param implementedInterface	Interface implémentée par le connecteur
	 * @param methodName			Nom de la méthode qu'il doit implémenter
	 * @param argCount				Nombre d'arguments de la méthode
	 * @return						l'implémentation de la méthode par le connecteur
	 */
	private static String getConnectorMethodBody(Class<?> implementedInterface, String methodName, int argCount) {
		
		String result = "return ((" + implementedInterface.getCanonicalName() + ")this.offering)." + methodName + "(";
		
		for (int i = 0; i < argCount; i++) {
			result += "p" + i;

			if (i < (argCount-1)) // last one
				result += ", ";
		}
		
		result += ");";
		
		return result;
	}
	
}
