package me.djalil.scoreboard.components;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import me.djalil.scoreboard.model.LightGame;

// TODO: Deferenciate between enemies and allies (and the participant himself)
public class KScoreboard extends JPanel {

	private LightGame game;
	
	public KScoreboard(LightGame game) {
		this.game = game;
		
		init();
	}
	
	private void init() {
		// Add margin
		var marginBottom = 75;
		this.setBorder(new EmptyBorder(0, 0, marginBottom, 0));
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		this.setBackground(KColor.AIR);
		
		var blue = game.getBlueParticipants();
		var kBlueList = new KParticipantList(blue);

		var red = game.getRedParticipants();
		var kRedList = new KParticipantList(red);

		this.add(kBlueList);
		this.add(kRedList);
	}
	
}
