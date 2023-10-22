package me.djalil.scoreboard.components;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import me.djalil.scoreboard.AppController;
import me.djalil.scoreboard.model.LightGame.Participant;

/**
 * Re-orderable.
 */
public class KParticipant extends JPanel {
	
	static final Dimension size = new Dimension(450, 60); 
	
	private Participant participant;

	public KParticipant(Participant participant) {
		super();
		
		this.participant = participant;

		init();
	}

	/**
	 * A la CSS
	 * 
	 * SEE https://developer.mozilla.org/en-US/docs/Web/CSS/visibility
	 */
	public static int VISIBILITY_VISIBLE = 1;
	public static int VISIBILITY_HIDDEN = 0;

	/*
	int visibility = VISIBILITY_VISIBLE; 	

	@Override
	public void paint(Graphics g) {
		if (visibility == VISIBILITY_VISIBLE) {
			super.paint(g);
		} else {
			// nothing
		}
	}
	*/
	
	private void init() {		
		this.setBorder(LineBorder.createGrayLineBorder());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setSize(size);
		this.setPreferredSize(size);
		this.setMinimumSize(size);
		this.setMaximumSize(size);
		//this.setBackground(KColor.AIR);
		this.setBackground(KColor.withAlpha(Color.WHITE, 10));

		// TODO: Refactor
		if (AppController.getAppModel().getGame().getBlueParticipants().contains(participant)) {
			var padding = new EmptyBorder(0, 25, 0, 0);
			var newBorder = new CompoundBorder(
					getBorder(),
					padding
					);
			setBorder(newBorder);
		}

		var kName = new KHeader(3, participant.summonerName);
		var kSpell1 = new KSpell(participant, 0);
		var kSpell2 = new KSpell(participant, 1);

		this.add(kName);
		this.add(kSpell1);
		this.add(kSpell2);
	}
}
