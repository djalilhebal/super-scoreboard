package me.djalil.scoreboard.services;

import java.util.List;

import me.djalil.scoreboard.model.LightGame;

public interface IGameProvider {

	/**
	 * Returns {region, summonerName} or null
	 */
	default List<String> getPrincipal() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * Current participant's live game.
	 */
	default LightGame getLiveGame() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * From a serialized (internal) JSON.
	 * For example, the original response of LiveClientData. 
	 * 
	 * Only used for dev.
	 * @param json
	 * @return
	 */
	default LightGame getLiveGame(String json) {
		throw new UnsupportedOperationException("Not implemented.");
	}
	
	/**
	 * By summoner name and region (e.g. "euw").
	 * @param region
	 * @param summonerName
	 */
	default LightGame getLiveGame(String region, String summonerName) {
		throw new UnsupportedOperationException("Not implemented.");
	}
}
