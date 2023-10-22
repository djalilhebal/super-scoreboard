package me.djalil.scoreboard.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

import me.djalil.scoreboard.Utils;

/**
 * The Thing. Everything is a Thing, obvio.
 * 
 * <b>A la JavaScript?</b>
 * <li>Kinda inspired by Schema.org
 * <li>and SolidJS's signals (e.g. `count()`
 * <li>and `setCount(val)`). and JQuery's attributes (e.g. `$el.height()` and
 * `$el.height(val)`).
 * 
 * <p>
 * Trying to make references to props nicer. <br>
 * Approaches:
 * <ul>
 * 		<li> 1. onChange(Method) with onChange(this::getName) doesn't work because "java target type of this
 * 				expression must be a functional interface".
 * 		<li> 2. onChange(Supplier) doesn't work because we can't access the original method's name.
 * 		<li> 3. Thought of using something like JavaScript's Symbols or wrapper classes, but nah, too messy.
 * </ul>
 * 
 * <pre>{@code
 * public void onChange(Supplier getter, PropertyChangeListener listener) {
 * 	// String attr = getter.getName();
 * 	String attr = getter.toString();
 * 	pcs.addPropertyChangeListener(attr, listener);
 * }
 * 
 * private void fireChange(Supplier getter, Object oldVal, Object newVal) {
 * 	String attr = getter.toString();
 * 	// String attr = getter.getName();
 * 	pcs.firePropertyChange(attr, oldVal, newVal);
 * }
 * }</pre>
 */
public class Thing {

	// TESTING
	public static void main(String[] args) {
		var thing = new Thing();
		thing.onChange(() -> System.out.println("somethingChange"));
		thing.onChange("name", () -> System.out.println("name changed"));
		thing.onChange("id", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.printf("id changed: prop=%s old=%d new=%d\n", evt.getPropertyName(), evt.getOldValue(),
						evt.getNewValue());
			}

		});
		thing.setName("Alice");
		thing.setName("Bob");
		thing.setId(2);
		thing.setId(3);
	}

	// ---

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * Any change.
	 */
	public void onChange(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Any change.
	 */
	public void onChange(Runnable runnable) {
		pcs.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				runnable.run();
			}

		});
	}

	public void onChange(String propName, PropertyChangeListener listener) {
		Utils.requireExists(this, propName);
		
		pcs.addPropertyChangeListener(propName, listener);
	}

	public void onChange(String propName, Runnable runnable) {
		Utils.requireExists(this, propName);

		pcs.addPropertyChangeListener(propName, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				runnable.run();
			}

		});
	}

	public void fireChange(String propName, Object oldVal, Object newVal) {
		Utils.requireExists(this, propName);

		pcs.firePropertyChange(propName, oldVal, newVal);
	}

	// ---

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		var oldVal = this.name;
		var newVal = name;
		this.name = name;
		fireChange("name", oldVal, newVal);
	}

	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		var oldVal = this.id;
		var newVal = id;
		this.id = id;
		fireChange("id", oldVal, newVal);
	}

}
