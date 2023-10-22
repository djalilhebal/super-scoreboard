package me.djalil.scoreboard.components;

import java.util.Map;

import javax.swing.JLabel;

/**
 * Named a la HTML Header.
 */
public class KHeader extends JLabel {

	static final Map<Integer, Float> map = Map.of(
			1, 20f,
			2, 16f,
			3, 10f
			);
	
	public KHeader(int depth, String text) {
		if (map.get(depth) != null) {
			setFontSize(map.get(depth));
		}
		
		setText(text);
    }
    
    void setFontSize(float size) {
    	this.setFont(this.getFont().deriveFont(size));
    }
}
