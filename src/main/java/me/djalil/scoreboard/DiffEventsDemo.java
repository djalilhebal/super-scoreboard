package me.djalil.scoreboard;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.StrokeBorder;

/**
 * Experiment.
 * 
 * Basic KParticipant interactions:
 * 
 * - Popup menu
 * 
 * - Double click
 * 
 * - Long click
 * 
 * - Holding indicator
 */
public class DiffEventsDemo {

	public static void main(String args[]) {
		DiffEventsDemo demo = new DiffEventsDemo();
	}

	boolean tabPressed = false;

	void onTabbing(boolean pressed) {
		this.tabPressed = pressed;
		System.out.println("Tabbing: " + pressed);
	}

	DiffEventsDemo() {
		JFrame frame = new JFrame();

		frame.setFocusTraversalKeysEnabled(false);
		frame.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(e);
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					onTabbing(true);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					onTabbing(false);
				}
			}

		});

		KParticipant p = new KParticipant();
		KSpell spell1 = new KSpell("");
		KSpell spell2 = new KSpell("");
		p.add(spell1);
		p.add(spell2);

		// frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		frame.setLayout(null);
		frame.add(p);

		frame.setSize(450, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

class KParticipant extends JPanel {
	private static final Dimension dims = new Dimension(715, 85);

	public KParticipant() {
		this.setSize(dims);
		this.setPreferredSize(dims);
		this.setBackground(Color.LIGHT_GRAY);

		setupEventListeners();
	}

	private void setupEventListeners() {
		JPopupMenu menu = new JPopupMenu();

		// Role
		JMenuItem roleSectionItem = new JMenuItem("Set role");
		roleSectionItem.setEnabled(false);
		menu.add(roleSectionItem);
		List.of("top", "jungle", "middle", "bottom", "support").forEach(roleName -> {
			menu.add(new AbstractAction(roleName) {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Swapping with role " + roleName);
				}

			});
		});

		this.setComponentPopupMenu(menu);
	}

}

class KSpell extends JButton {
	private static final Dimension dims = new Dimension(35, 35);

	public KSpell(String text) {
		super(text);

		this.setFocusable(false);
		this.setSize(dims);
		this.setPreferredSize(dims);
		this.setOpaque(false);
		this.setContentAreaFilled(false);

		setupEventListeners();
	}

	private void onDoubleClick() {
		log("onDoubleClick;" + this.getText());
	}

	private void onLongClick() {
		log("onLongClick;" + this.getText());
	}

	private void setupEventListeners() {
		this.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
					onDoubleClick();
				}
			}

		});

		var originalBorder = KSpell.this.getBorder();

		this.addMouseListener(new LongPressAdapter() {

			@Override
			public void longPressing(double progress) {
				if (progress < 0) {
					KSpell.this.setBorder(originalBorder);
					return;
				}

				if (progress > 1)
					progress = 1;
				/*
				 * if (progress >= 1) { System.out.println("Long pressing COMPLETED"); } else if
				 * (progress < 0) { System.out.println("Long pressing STOPPED"); } else {
				 * System.out.println("Long pressing PROGRESS " + progress); }
				 */

				long len = 140;
				float dashPhase = (1 - (float) progress) * (float) len;
				float dash[] = { (float) len };
				BasicStroke dashedStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 2f, // miter
																													// limit
						dash, dashPhase);

				KSpell.this.setBorder(new StrokeBorder(dashedStroke, Color.MAGENTA));

			}

			@Override
			public void longPressed() {
				onLongClick();
			}
		});
	}

	private static void log(Object x) {
		System.out.println(x);
	}

}

abstract class LongPressAdapter extends MouseAdapter {

	/**
	 * Progress from [0; 1] or -1 if we are no long pressing it. Updated every
	 * {@code longPressingInterval} millis.
	 * 
	 * @param progress
	 */
	abstract public void longPressing(double progress);

	abstract public void longPressed();

	// ---

	private long mousePressedTime;

	// in millis
	private int longPressDuration = 750;
	private int longPressingInterval = 25;

	private Timer longPressTimer = new Timer(longPressDuration, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			longPressed();
		}

	});

	private Timer longPressingTicker = new Timer(longPressingInterval, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			long nowMillis = Instant.now().toEpochMilli();
			long diff = nowMillis - mousePressedTime;
			double progress = diff / (double) longPressDuration;

			// System.out.printf("now - pressed = %d - %d = %d (%f)\n", nowMillis,
			// mousePressedTime, diff, progress);
			longPressing(progress);
		}

	});

	private void setup() {
		longPressTimer.setRepeats(false);
		longPressingTicker.setRepeats(true);

		longPressTimer.restart();
		longPressingTicker.restart();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousePressedTime = e.getWhen();
		setup();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		longPressing(-1);
		longPressTimer.stop();
		longPressingTicker.stop();
	}

}
