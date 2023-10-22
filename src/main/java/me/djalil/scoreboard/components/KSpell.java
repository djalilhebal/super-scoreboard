package me.djalil.scoreboard.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.time.Instant;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.StrokeBorder;

import me.djalil.scoreboard.App;
import me.djalil.scoreboard.AppController;
import me.djalil.scoreboard.model.AppModel;
import me.djalil.scoreboard.model.LightGame;
import me.djalil.scoreboard.model.SpellTiming;
import me.djalil.scoreboard.model.LightGame.Participant;

/**
 * TODO:
 * - [ ] It doesn't have to be a {@link JButton}.
 * We are not using its model or actions. We are even overriding most of its default styles.
 * - [ ] {@link KSpell} and {@link KSpellTimer} can be merged.
 */
public class KSpell extends JButton {
	static final Logger LOG = Logger.getLogger(KSpell.class.getName());

	static final Dimension defaultSize = new Dimension(25, 25);

	final static int borderWidth = 2;
	final static Border inactiveBorder = new LineBorder(KColor.withAlpha(KColor.GRAY, 150), borderWidth);
	final static Border activeBorder = new LineBorder(KColor.PURPLE, borderWidth);
	final static Color holdingBorderColor = Color.YELLOW;
	Border holdingBorder = null;

	// TODO/Refactor: We should be calling the controller, not the model.
	private AppModel appModel;
	
	private LightGame game;
	private Participant participant;
	private int spellIndex;

	private KSpellTimer kSpellTimer;
	
	public KSpell(Participant participant, int spellIndex) {
		super();

		var name = String.format("KSpell.%s.%s", participant.summonerName, spellIndex);
		this.setName(name);

		this.appModel = App.getAppModel();

		this.game = appModel.getGame();
		this.participant = participant;
		this.spellIndex = spellIndex;

		init();
	}

	private void init() {
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		this.setSize(defaultSize);
		this.setPreferredSize(defaultSize);
		this.setMaximumSize(defaultSize);
		this.setOpaque(false);
		this.setFocusable(false);
		this.setBorder(inactiveBorder);
		//this.setLayout(null);

		this.setContentAreaFilled(false);
		// This doesn't change much, does it?
		this.setMargin(new Insets(0, 0, 0, 0));

		kSpellTimer = new KSpellTimer();
		this.add(kSpellTimer);

		setupEventListeners();

		game.onChange("duration", () -> {
			// LOG.info("Game time changed. Updater KSpellTimer.");
			SwingUtilities.invokeLater(() -> {
				refreshAll();
			});
		});
	}
	
	private void refreshAll() {
		var spellTiming = appModel.getSpellUsage(participant.summonerName, spellIndex);
		kSpellTimer.onElapsedTime(spellTiming);

		if (holdingBorder != null) {
			this.setBorder(holdingBorder);
		} else if (spellTiming.getElapsedRatio() < 1) {
			this.setBorder(activeBorder);
		} else {
			this.setBorder(inactiveBorder);
		}		
	}

	private void onDoubleClick() {
		LOG.info("onDoubleClick;" + this.getName());
		appModel.recordSpellUsage(participant.summonerName, spellIndex);

		// Repaint ASAP
		refreshAll();
	}

	private void onLongClick() {
		LOG.info("onLongClick;" + this.getName());
		appModel.clearSpellUsage(participant.summonerName, spellIndex);

		// Repaint ASAP
		refreshAll();
	}

	private void setupEventListeners() {
		this.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
					onDoubleClick();
				}
			}

		});

		this.addMouseListener(new LongPressAdapter() {

			@Override
			public void longPressing(double progress) {

				if (progress < 0) {
					// Long pressing STOPPED/CANCELLED
					holdingBorder = null;
					refreshAll();
					return;
				} else if (progress >= 1) {
					// Long pressing COMPLETED
					// Normalize the value.
					progress = 1;
				} else {
					// Long pressing PROGRESS
					// The value is in range [0; 1], so do nothing.
				}
				
				long len = KSpell.this.getSize().width * 4;
				float dashPhase = (1 - (float) progress) * (float) len;
				float dash[] = { (float) len };
				BasicStroke dashedStroke = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, borderWidth, // miter
																													// limit
						dash, dashPhase);

				holdingBorder = new StrokeBorder(dashedStroke, holdingBorderColor);
				// refeshBorder
				refreshAll();
			}

			@Override
			public void longPressed() {
				onLongClick();
			}

		});
	}

}

@SuppressWarnings("serial")
class KSpellTimer extends JComponent {

	private static Color backgroundColor = KColor.withAlpha(KColor.INDIGO, 200);
	private static Color textColor = KColor.YELLOW;

	private SpellTiming spellTiming = null;
	private double progressValue = 1;

	public KSpellTimer() {
		//this.setLocation(0, 0);
		this.setOpaque(false);
	}
	
	// TODO: Should be changeable (public static?).
	private String template = REMAINING_TEMPLATE;

	/**
	 *  When will it be up (ingame timestamp)?
	 */
	final static String TIMESTAMP_TEMPLATE = "~MM:SS";
	/**
	 *  Seconds remaining until it's up
	 */
	final static String REMAINING_TEMPLATE = "-SSS";
	/**
	 *  Calculated cooldown. For debugging.
	 */
	final static String COOLDOWN_TEMPLATE = "=SSS";

	public void onElapsedTime(SpellTiming spellTiming) {
		if (this.progressValue != spellTiming.getElapsedRatio()) {
			this.spellTiming = spellTiming;
			this.progressValue = spellTiming.getElapsedRatio();

			this.repaint();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if (getParent() != null) {
			return getParent().getPreferredSize();
		}
		return new Dimension(25, 25);
	}

	@Override
	protected void paintComponent(Graphics g) {
		var g2 = (Graphics2D) g;
		
		/*
		var parent = (JButton) getParent();
		System.out.println("parent.getMargin() " + parent.getMargin());
		System.out.println("parent.getInsets() " + parent.getInsets());
		System.out.println("parent.getBounds() " + parent.getBounds());
		System.out.println("parent.getSize() " + parent.getSize());
		System.out.println("this.getSize() " + this.getSize());		
		System.out.println();
		*/

		var innerSize = getSize();
		var clippingRect = new Rectangle(innerSize.width, innerSize.height);
		var progressArc = new Arc2D.Double(Arc2D.PIE);

		g2.clearRect(0, 0, clippingRect.width, clippingRect.height);
		g2.setColor(KColor.withAlpha(Color.WHITE, 10));
		g2.fill(clippingRect);

		if (progressValue == 1) {
			return;
		}

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

		g2.setStroke(new BasicStroke(0.5f));
		g2.setColor(Color.WHITE);
		g2.draw(progressArc);

		String text;
		switch (template) {
			case REMAINING_TEMPLATE:
				text = "-" + spellTiming.getRemainingSeconds();
				break;
			case TIMESTAMP_TEMPLATE:
				text = "~" + spellTiming.getWhenUpIngame();
				break;
			case COOLDOWN_TEMPLATE:
				text = "=" + (int)spellTiming.cooldown;
				break;
			default:
				throw new IllegalStateException();
		}

		// FIXME
		//setStretchFont(template);
		// For some reason `getFont().getSize()`'s value changes from 12 to 13.
		//System.out.println("getFont().getSize() " + getFont().getSize());
		setFontSize(8);
		
		g2.setColor(textColor);
		g2.drawString(text, 0, clippingRect.height - getFont().getSize());
		//drawCenteredString(g2, text, getBounds());
	}

	/**
	 * Draw a string centered in the middle of a Rectangle.
	 * 
	 * - Adapted from https://stackoverflow.com/a/27740330
	 * - [ ] Check https://stackoverflow.com/a/14284949
     *
	 * @param g The Graphics instance.
	 * @param text The String to draw.
	 * @param rect The Rectangle to center the text in.
	 */
	public void drawCenteredString(Graphics g, String text, Rectangle rect) {
	    // Get the FontMetrics
	    FontMetrics metrics = g.getFontMetrics();
	    // Determine the X coordinate for the text
	    int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
	    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
	    int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
	    // Draw the String
	    g.drawString(text, x, y);
	}
	
	void setFontSize(int newFontSize) {
		var newFont = this.getFont().deriveFont((float) newFontSize);
		this.setFont(newFont);
	}

	// Adapted from https://stackoverflow.com/a/2715279
	void setStretchFont(String str) {
		var font = getFont();
		int stringWidth = this.getFontMetrics(font).stringWidth(str);
		int componentWidth = this.getWidth();

		// Find out how much the font can grow in width.
		double widthRatio = (double) componentWidth / (double) stringWidth;

		int newFontSize = (int) (font.getSize() * widthRatio);
		int componentHeight = this.getHeight();

		// Pick a new font size so it will not be larger than the height of label.
		int fontSizeToUse = Math.min(newFontSize, componentHeight);

		// Set the label's font size to the newly determined size.
		var newFont = font.deriveFont((float) fontSizeToUse);
		this.setFont(newFont);
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
	private int longPressDelay = 200;
	private int longPressDuration = 750;
	private int longPressingInterval = 25;

	private Timer longPressTimer = new Timer(longPressDuration + longPressDelay, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			longPressed();
		}

	});

	private Timer longPressingTicker = new Timer(longPressingInterval, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			long nowMillis = Instant.now().toEpochMilli();
			long diff = nowMillis - mousePressedTime - longPressDelay;
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
