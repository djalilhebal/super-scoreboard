package me.djalil.scoreboard.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import me.djalil.scoreboard.AppController;
import me.djalil.scoreboard.model.LightGame;
import me.djalil.scoreboard.model.LightGame.Participant;

public class KParticipantList extends JPanel {

	private LightGame game;
	private Map<String, KParticipant> map;
	private List<Participant> participants;
	
	public KParticipantList(List<Participant> participants) {
		game = AppController.getAppModel().getGame();
		map = new HashMap<>();
		this.participants = participants;
		
		init();
	}

	private void init() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(KColor.AIR);

		participants.forEach(p -> {
			var key = p.summonerName;
			var kParticipant = new KParticipant(p);
			map.put(key, kParticipant);
			this.add(kParticipant);
		});
		onReordered();

		game.onChange("participantsOrder", () -> {
			SwingUtilities.invokeLater(() -> onReordered());
		});
		
	}

	private void onReordered() {
	    this.removeAll();

	    this.game
	        .getParticipantsOrder()
	        .forEach(summonerName -> {
	            var c = map.get(summonerName);
	            if (c != null) {
		            this.add(c);
	            }
	        });
	    
	    this.revalidate();
	}

	// Manual
    public void moveChildTo(int index, Component c) {
        this.remove(c);
        this.add(c, index);
        
        // This works
        revalidate();
        
        // This too
        //invalidate();
        //validate();
        //repaint();
    }

	
}
