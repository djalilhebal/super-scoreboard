package me.djalil.scoreboard.services;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

import me.djalil.scoreboard.Utils;
import me.djalil.scoreboard.model.LightGame;

public class OpggService implements IGameProvider {

	private static final Gson gson = new Gson();
	private static final Map<String, String> cache = new HashMap<>();
	private static final Logger LOG = Logger.getLogger(OpggService.class.getName());

	public static void main(String[] args) {

		var region = "na";
		var summonerName = "QuinnAD";

		var opggGame = getGameData(region, summonerName);
		if (opggGame != null) {
			var game = opggGame.toLight();
			System.out.println("LightGame");
			System.out.println(game);
			System.out.println();
		} else {
			// Probably not ingame
			// or opgg's servers haven't caught up yet.
			// Either way, we need to retry or use a fallback.
			System.out.println("No game");
		}

	}

	// ===
	
	@Override
	public LightGame getLiveGame(String region, String summonerName) {
		var opggGame = getGameData(region, summonerName);
		return opggGame.toLight();
	}

	@Override
    public LightGame getLiveGame(String body) {
		OpggLiveGameResponse opggGame = gson.fromJson(body, OpggLiveGameResponse.class);
		return opggGame.toLight();
	}

	public static OpggLiveGameResponse getGameData(String region, String summonerName) {
		var cacheKey = summonerName + "#" + region;
		var encryptedSummonerId = cache.computeIfAbsent(cacheKey, (key) -> {
			LOG.info("Fetching id for key " + cacheKey);
			return getSummonerId(region, summonerName);
		});

		var gameData = getGameDataByEncryptedSummonerId(region, encryptedSummonerId);
		return gameData;
	}

	/**
	 * Get OP.GG's `encryptedSummonerId`.
	 *
	 * Example: <code></pre>
	 * actual = getSummonerId("euw", "ExactlyOnce");
	 * expected = "p6Um_byTsImq_vbeJUQo1P-7gioW2p4hRCdbL2T7dSY1YZN0bL9eHk6AMQ";
	 * assert.equal(actual, expected);
	 * </pre></code>
	 */
	private static String getSummonerId(String region, String summonerName) {
		final String urlTemplate = "https://op.gg/api/v1.0/internal/bypass/summoners/%s/autocomplete?keyword=%s";
		String url = String.format(urlTemplate, region, summonerName);

		// TODO/Refactor: Get rid of JsonPath, and use Gson + OpggAutocompleResponse
		final String jsonPath = "$.data[0].summoner_id";
		// JSON
		String res = Utils.fetch(url);
		// assert that res.data's length == 1;
		String encryptedSummonerId = JsonPath.parse(res).read(jsonPath);
		return encryptedSummonerId;
	}

	private static OpggLiveGameResponse getGameDataByEncryptedSummonerId(String region, String encryptedSummonerId) {
		final String urlTemplate = "https://op.gg/api/v1.0/internal/bypass/spectates/%s/%s?hl=en_US";
		String url = String.format(urlTemplate, region, encryptedSummonerId);

		String body = Utils.fetch(url);

		try {
			String errorJsonPath = "$.code";
			int val = JsonPath.parse(body).read(errorJsonPath);
			boolean isNotFound = val == 404;
			if (isNotFound) {
				return null;
			}
		} catch (com.jayway.jsonpath.PathNotFoundException ignoredEx) {
		}

		try {
			OpggLiveGameResponse result = gson.fromJson(body, OpggLiveGameResponse.class);
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
