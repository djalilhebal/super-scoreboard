package me.djalil.scoreboard.services;

import java.util.List;
import java.util.stream.Collectors;

import me.djalil.scoreboard.model.LightGame;

/**
 * Response type of https://127.0.0.1:2999/liveclientdata/allgamedata
 */
public class LiveClientDataResponse {
    public List<Player> allPlayers;
    public GameData gameData;

    public LightGame toLight() {
        var result = new LightGame();
        
        result.participants = this.allPlayers.stream()
            .map(p -> {
                var newP = new LightGame.Participant();
                newP.summonerName = p.summonerName;
                newP.team = p.team.equals("ORDER") ? "BLUE" : "RED";
                newP.mainRuneIds = List.of(
                		p.runes.keystone.id,
                		p.runes.primaryRuneTree.id,
                		p.runes.secondaryRuneTree.id
        		);
                newP.itemIds = p.items.stream().map(x -> x.itemID).collect(Collectors.toList());
                return newP;
            })
            .collect(Collectors.toList());
        
        result.participantsOrder = this.allPlayers.stream().map(p -> p.summonerName).collect(Collectors.toList());

        return result;
    }

    // ---
    
    public static class GameData {
        public double gameTime;
    }

    public static class Player {
    	public String summonerName;
    	
        /**
         * "ORDER" or "CHAOS"
         */
        public String team;

        /**
         * Values: "TOP", "JUNGLE", "MIDDLE", "BOTTOM", or "UTILITY".
         */
        public String position;

        public RuneMap runes;
        
        public List<Item> items;

    }

    public static class Item {
        public int itemID;
    }
    
    public static class Rune {
    	public int id;
    	public String displayName;
        public String rawDisplayName;
    }
    
    public static class RuneMap {
    	public Rune keystone;
    	public Rune primaryRuneTree;
    	public Rune secondaryRuneTree;
    }
}
