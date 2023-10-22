package me.djalil.scoreboard;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.keyboard.SwingKeyAdapter;

/**
 * Responsible for converting global input events (mouse and keyboard) to Swing
 * events.
 * 
 * - Invokes corresponding event listener methods.
 *
 * - Uses https://github.com/kwhat/jnativehook
 */
@Deprecated
public class EventSynthesizer {

	/**
	 * Forward events
	 * 
	 * - FIXME: For some reason, `eventQueue.postEvent(keyEvent)` and `component.dispatchEvent(keyEvent)` don't seem to work,
	 * even after changing the event's `source`. 
	 * 
	 * @throws IllegalArgumentException if {@code targetComponent} does not implement {@link KeyListener}.
	 */
	public static void forwardKeyboardEvents(Component targetComponent) {
		if (targetComponent.getKeyListeners().length != 1) {
			throw new IllegalArgumentException();
		}
		
		KeyListener target = targetComponent.getKeyListeners()[0];

		NativeKeyListener listener = new SwingKeyAdapter() {

			@Override
			public void keyTyped(KeyEvent keyEvent) {
				//keyEvent.setSource(target);
				target.keyTyped(keyEvent);
			}

			@Override
			public void keyPressed(KeyEvent keyEvent) {
				//keyEvent.setSource(target);
				target.keyPressed(keyEvent);
			}

			@Override
			public void keyReleased(KeyEvent keyEvent) {
				//keyEvent.setSource(target);
				target.keyReleased(keyEvent);
			}
		};

		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			ex.printStackTrace();
			System.err.println("There was a problem registering the native hook.");
			System.exit(1);
		}

		System.out.println("addNativeKeyListener");
		GlobalScreen.addNativeKeyListener(listener);
	}
}
