package me.djalil.scoreboard;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

public class Surge {

	/***
	 * Get the context's value.
	 * 
	 * - It is expected clazz is context-provider-ish.
	 * Meaning, it is like a Supplier (`get`) but the getter can be named anything  (e.g. getGame).
	 * 
	 * - XXX: Maybe make it return an `Optional`?
	 *  
	 * @return the context's value or null
	 */
	public static <U> U useContext(Class<?> clazz, Component thiz) {
		// Assert context-ish
		// XXX: A functional interface can have more than one declared method if others have default implementations.
	    //assert clazz.isAnnotationPresent(FunctionalInterface.class);
	    Method[] meths = clazz.getDeclaredMethods();
	    if (!(meths.length == 1 && meths[0].getParameterCount() == 0)) {
	    	throw new IllegalArgumentException(
	    			String.format("The context class '%s' must declare exactly one method (the getter) that takes 0 arguments.", clazz.getName())
	    			);
	    }

	    // Find the context
		Component context = SwingUtilities.getAncestorOfClass(clazz, thiz);
		if (context == null) {
			System.out.println("[useContext] Context provider not found. Returning null.");
			return null;
		}

	    // Get its value
		try {
		    Method getter = meths[0];
		    Object ret = getter.invoke(context);
		    return (U)ret;
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			System.err.println("[useContext] Unexcepted error.");
			e.printStackTrace();
			return null;
		}
	}

}
