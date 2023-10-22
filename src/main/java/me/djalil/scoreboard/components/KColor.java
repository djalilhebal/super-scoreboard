package me.djalil.scoreboard.components;

import java.awt.Color;

public class KColor extends Color {

	public KColor(int r, int g, int b, int a) {
		super(r, g, b, a);
	}

	public static Color withAlpha(Color c, int alpha) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	// --- NAMED COLORS ---

	public static final Color PURPLE = new Color(128, 0, 128);
	public static final Color INDIGO = new Color(75, 0, 130);
	public static final Color ROSYBROWN = new Color(188, 143, 143);

	/**
	 * CSS color transparent 
	 */
	public static final Color AIR = new Color(0, 0, 0, 0);

}
