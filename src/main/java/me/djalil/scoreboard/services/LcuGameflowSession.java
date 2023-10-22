package me.djalil.scoreboard.services;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import me.djalil.scoreboard.model.LightGame;

/**
 * Response type of the LCU (HTTP and WebSocket).
 * {@code GET /lol-gameflow/v1/session}
 */
public class LcuGameflowSession {
    /**
     * It seems like the only diff between GameStart and InProgress is that `gameClient.running` is true.
     * See {@link Phase}
     */
    public String phase;
    public GameData gameData;

    public LightGame toLight() {
        var ret = new LightGame();
        ret.gameId = this.gameData.gameId;
        ret.participants = this.gameData.playerChampionSelections.stream()
            .map(p -> {
            	var newP = new LightGame.Participant();
            	newP.summonerName = p.summonerInternalName;
            	newP.spellIds = List.of(p.spell1Id, p.spell2Id);
            	/*
            	// FIXME: Bots don't have `summonerInternalName`
            	var isBlue = this.gameData.teamOne.stream().anyMatch(x -> x.summonerName.equals(p.summonerInternalName));
            	newP.team = isBlue ? "BLUE" : "RED";
            	*/
                return newP;
            })
            .collect(Collectors.toList());
        
        return ret;
    }

    // ---
    
    public static enum Phase {
        None,
    	Lobby,
    	Matchmaking,
    	ReadyCheck,
    	ChampSelect,
        GameStart,
    	InProgress,
    	// ...
    }

    // ---
    
    public static class GameData {
        /**
         * Game ID or 0 if no game is running.
         */
        public long gameId;
        
        public List<PlayerChampionSelection> playerChampionSelections;
        /**
         * BLUE.
         */
        public List<Participant> teamOne;
        /**
         * RED.
         */
        public List<Participant> teamTwo;
    }
    
    public static class PlayerChampionSelection {
    	public int championId;
    	public int spell1Id;
    	public int spell2Id;
    	public String summonerInternalName;
    }

    public static class Participant {
    	public int championId;
    	/**
    	 * Position.
    	 */
    	public String selectedPosition;
    	public String summonerName;
    }
    
}
