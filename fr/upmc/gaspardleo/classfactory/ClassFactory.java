package fr.upmc.gaspardleo.classfactory;

import fr.upmc.components.connectors.AbstractConnector;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class ClassFactory {
	
	private static int id = 0;
	
	public static Class<?> newConnector(Class<?> implementedInterface) 
			throws NotFoundException, CannotCompileException {
		
		return newConnector(implementedInterface.getName() + "Connector" + (id++), 
				AbstractConnector.class, implementedInterface);
	}
	
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
