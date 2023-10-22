package me.djalil.scoreboard.services;

import com.google.gson.Gson;

import me.djalil.scoreboard.Utils;
import me.djalil.scoreboard.model.LightGame;

public class LiveClientDataService implements IGameProvider {
	private final Gson gson = new Gson();
	
	@Override
	public LightGame getLiveGame() {
		try {
			var url = "https://127.0.0.1:2999/liveclientdata/allgamedata";
			var body = Utils.fetchInsecure(url);
			LiveClientDataResponse game = gson.fromJson(body, LiveClientDataResponse.class);
			//System.out.println("LCD raw body " + (body == null ? null : body.substring(0, 100)));
			//System.out.println("LCD parsed body " + gson.toJson(game));
			return game == null ? null : game.toLight();
		} catch (Exception ex) {
			//ex.printStackTrace();
			return null;
		}
	}

	@Override
    public LightGame getLiveGame(String body) {
		LiveClientDataResponse game = gson.fromJson(body, LiveClientDataResponse.class);
		return game.toLight();
	}

}
