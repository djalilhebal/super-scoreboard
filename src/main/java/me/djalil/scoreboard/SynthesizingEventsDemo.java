package me.djalil.scoreboard;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Arc2D;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

/**
 * Experiment.
 * 
 * - Drawing the inner progress indicator.
 * 
 * - Other junk...
 */

class SpellTimingUpdate {
	/**
	 * Seconds in-game
	 */
	public long whenUsedIngame;
	/**
	 * Seconds, Unix time.
	 */
	public long whenUsed;
	/**
	 * Seconds in-game
	 */
	public long whenUpIngame;
	/**
	 * Seconds, Unix time.
	 */
	public long whenUp;

	// Important but private? info
	public int originalTotalCd;
	// ATM of recording
	public int totalSpellHaste;
	// ATM of recording, after applying hasted to originalTotalCd
	public int totalCd;

	// calculated
	// from Now.
	public float elapsedRatio;
	// (whenUp - whenUsed) / totalCd
}

@SuppressWarnings("serial")
class KSpellTimer extends JPanel {
	public double progressValue = 0.6;
	public String nextTimestamp = "12:54";

	// Or, KColor.from(Color.gray).withAlpha(100);
	private static Color backgroundColor = new Color(0, 0, 0, 50);
	private static Color textColor = Color.yellow;

	public KSpellTimer() {
		this.setBackground(Color.WHITE);
	}

	public void onElapsedTime(SpellTimingUpdate spellTiming) {
		this.progressValue = spellTiming.elapsedRatio;
		this.nextTimestamp = spellTiming.whenUpIngame + "s";
		this.repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100, 100);
	}

	@Override
	protected void paintComponent(Graphics g) {
		var g2 = (Graphics2D) g;

		var clippingRect = new Rectangle(100, 100);
		var progressArc = new Arc2D.Double(Arc2D.PIE);

		// "To fit rectangle into a circle, we need the circle to have
		// a diameter greater than or equal to the diagonal of the rectangle"
		var w = clippingRect.width;
		var h = clippingRect.height;
		double diagonal = Math.sqrt(w * w + h * h);

		int startAngle = 90;
		int endAngle = (int) (360 * progressValue);
		progressArc.setArcByCenter(clippingRect.getCenterX(), clippingRect.getCenterY(), diagonal, startAngle,
				-endAngle, Arc2D.PIE);
		g2.setClip(clippingRect);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(backgroundColor);
		g2.fill(progressArc);

		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.WHITE);
		g2.draw(progressArc);

		g2.setColor(textColor);
		var text = "~" + nextTimestamp;
		var fontSize = 20;
		var font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
		g2.setFont(font);
		g2.drawString(text, 0, clippingRect.height - fontSize);
	}

}

public class SynthesizingEventsDemo extends JFrame
		implements NativeKeyListener, NativeMouseInputListener, WindowListener {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new SynthesizingEventsDemo();
			}
		});
	}
	
	private Component hoveredEl = null;
	private NativeMouseEvent latestMouseEvent = null;

	public SynthesizingEventsDemo() {
		// Set the event dispatcher to a swing safe executor service.
		GlobalScreen.setEventDispatcher(new SwingDispatchService());

		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

			@Override
			public void eventDispatched(AWTEvent event) {
				System.out.println("eventDispatched");
				System.out.println("event: " + event.paramString());
				System.out.println("source: " + event.getSource());

				System.out.println("");
			}
		}, AWTEvent.MOUSE_EVENT_MASK);

		setTitle("JNativeHook Demo");
		setSize(500, 250);
		setLocation(50, 50);
		setName("app");

		var pane = new JPanel();
		pane.setName("main-section");
		pane.setBackground(Color.green);
		pane.setBorder(BorderFactory.createLineBorder(Color.black));

		var mainLabel = new JLabel("Tea");
		mainLabel.setName("tea");
		mainLabel.setBorder(BorderFactory.createLineBorder(Color.black));
		var menu = new JPopupMenu();
		menu.add("A");
		menu.add("B");
		mainLabel.setComponentPopupMenu(menu);

		var exitButton = new JButton("Exit");
		exitButton.setBorderPainted(true);
		exitButton.setName("exit");
		exitButton.setBackground(Color.red);
		exitButton.setSize(100, 50);
		exitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});

		pane.add(exitButton);
		pane.add(mainLabel);
		var b = new JButton();
		b.setBackground(Color.white);
		b.add(new KSpellTimer());
		pane.add(b);
		this.add(pane);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(this);
		setVisible(true);
	}

	private static Window findWindow() {
		for (Window window : Window.getWindows()) {
			if (window.getMousePosition(true) != null)
				return window;
		}

		return null;
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent nativeEvent) {
		latestMouseEvent = nativeEvent;
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
		var point = nativeEvent.getPoint();
		var location = MouseInfo.getPointerInfo().getLocation();
		/*
		 * System.out.printf("nativeMouseMoved\n");
		 * System.out.printf("\tNativeEvent: %d, %d\n", point.x, point.y);
		 * System.out.printf("\tMouseInfo: %d, %d\n", location.x, location.y);
		 */

		var win = findWindow();
		if (win == null) {
			return;
		}

		var resultNative = SwingUtilities.getDeepestComponentAt(win, point.x, point.y);
		SwingUtilities.convertPointFromScreen(point, win);
		var resultNativeConverted = SwingUtilities.getDeepestComponentAt(win, point.x, point.y);
		var resultSwing = SwingUtilities.getDeepestComponentAt(win, location.x, location.y);
		SwingUtilities.convertPointFromScreen(location, win);
		var resultSwingConverted = SwingUtilities.getDeepestComponentAt(win, location.x, location.y);

		hoveredEl = resultSwingConverted;
		// latestMouseEvent = nativeEvent;

		/*
		 * show("resultNative", resultNative); show("resultNativeConverted",
		 * resultNativeConverted); show("resultSwing", resultSwing);
		 * show("resultSwingConverted", resultSwingConverted);
		 */

	}

	protected MouseEvent getJavaMouseEvent(NativeMouseEvent nativeEvent) {
		return new MouseEvent(this,
				nativeEvent.getID() - (NativeMouseEvent.NATIVE_MOUSE_FIRST - MouseEvent.MOUSE_FIRST),
				System.currentTimeMillis(), this.getJavaModifiers(nativeEvent.getModifiers()), nativeEvent.getX(),
				nativeEvent.getY(), nativeEvent.getClickCount(), false, nativeEvent.getButton());
	}

	@SuppressWarnings("deprecation")
	protected int getJavaModifiers(int nativeModifiers) {
		int modifiers = 0x00;
		if ((nativeModifiers & NativeInputEvent.SHIFT_MASK) != 0) {
			modifiers |= KeyEvent.SHIFT_MASK;
			modifiers |= KeyEvent.SHIFT_DOWN_MASK;
		}
		if ((nativeModifiers & NativeInputEvent.META_MASK) != 0) {
			modifiers |= KeyEvent.META_MASK;
			modifiers |= KeyEvent.META_DOWN_MASK;
		}
		if ((nativeModifiers & NativeInputEvent.CTRL_MASK) != 0) {
			modifiers |= KeyEvent.CTRL_MASK;
			modifiers |= KeyEvent.CTRL_DOWN_MASK;
		}
		if ((nativeModifiers & NativeInputEvent.ALT_MASK) != 0) {
			modifiers |= KeyEvent.ALT_MASK;
			modifiers |= KeyEvent.ALT_DOWN_MASK;
		}
		if ((nativeModifiers & NativeInputEvent.BUTTON1_MASK) != 0) {
			modifiers |= KeyEvent.BUTTON1_MASK;
			modifiers |= KeyEvent.BUTTON1_DOWN_MASK;
		}
		if ((nativeModifiers & NativeInputEvent.BUTTON2_MASK) != 0) {
			modifiers |= KeyEvent.BUTTON2_MASK;
			modifiers |= KeyEvent.BUTTON2_DOWN_MASK;
		}
		if ((nativeModifiers & NativeInputEvent.BUTTON3_MASK) != 0) {
			modifiers |= KeyEvent.BUTTON3_MASK;
			modifiers |= KeyEvent.BUTTON3_DOWN_MASK;
		}

		return modifiers;
	}

	private static void show(String title, Component x) {
		System.out.printf("\t * %s: %s#%s\n", title, x == null ? "NULL" : x.getClass().getSimpleName(),
				x == null ? "NULL" : x.getName());
	}

	public void windowOpened(WindowEvent e) {
		// Initialze native hook.
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
			ex.printStackTrace();

			System.exit(1);
		}

		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
		GlobalScreen.addNativeMouseMotionListener(this);

	}

	public void windowClosed(WindowEvent e) {
		// Clean up the native hook.
		try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e1) {
			e1.printStackTrace();
		}
		System.runFinalization();
		System.exit(0);
	}

	public void windowClosing(WindowEvent e) {
		/* Unimplemented */ }

	public void windowIconified(WindowEvent e) {
		/* Unimplemented */ }

	public void windowDeiconified(WindowEvent e) {
		/* Unimplemented */ }

	public void windowActivated(WindowEvent e) {
		/* Unimplemented */ }

	public void windowDeactivated(WindowEvent e) {
		/* Unimplemented */ }

	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VC_TAB) {
			// fromNativeKeyEvent(e, frame);
		}
	}

	public void nativeKeyReleased(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VC_TAB) {
			// Tabbing false
		}
	}

	public void nativeKeyTyped(NativeKeyEvent e) {
		/* Unimplemented */ }

}