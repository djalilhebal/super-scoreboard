package me.djalil.scoreboard;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

public interface SurgeUser {

	default <T> T useContext(Class<?> clazz) {
		return Surge.useContext(clazz, (Component) this);
	}

}
