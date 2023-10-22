package me.djalil.scoreboard;

import java.util.Optional;

import me.djalil.scoreboard.model.LightGame;
import me.djalil.scoreboard.model.LightGameUtils;
import me.djalil.scoreboard.services.*;

// scrap
public class ServicesDemo {

	public static void main(String args) {
		// Init services
		var lcuService = new LcuService();
		var liveClientDataService = new LiveClientDataService();
		var opggService = new OpggService();
		
		lcuService.onNewGame((object) -> {
			// General info about summoner spells (names and cooldowns)
			DdragonService.init();
			var spells = DdragonService.getSpells();

			var game = new LightGame();
			var lcuSessionGame = Optional.ofNullable(lcuService.getLiveGame());
			var lcuLiveGame = Optional.ofNullable(liveClientDataService.getLiveGame());
			//lcuService.getSummonerName();
			//lcuService.getRegion();
			var opggGame = Optional.ofNullable(opggService.getLiveGame("oce", "Incursio"));

			// Summoner spells of each player
			lcuSessionGame.ifPresent(game::merge);
			// Items
			lcuLiveGame.ifPresent(game::merge);
			// Runes
			opggGame.ifPresent(game::merge);

			// Now, `game` contains all the info we need.
			// If we need to update items, we get a new LightGame from the liveClientDataService and merge it.
		});
		
	}
	
}
