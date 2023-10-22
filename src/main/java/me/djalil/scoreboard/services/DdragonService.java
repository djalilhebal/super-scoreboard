package me.djalil.scoreboard.services;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import me.djalil.scoreboard.Utils;
import me.djalil.scoreboard.model.LightSpell;
import no.stelar7.api.r4j.impl.lol.raw.DDragonAPI;

public class DdragonService {
	private static Logger LOG = Logger.getLogger(DdragonService.class.getName());
	private static Gson gson = new Gson();

	private static String gamePatch = null;
	private static List<LightSpell> spells = null;
	
	// TESTING
	public static void main(String[] args) {
		init();
		
		System.out.println(gamePatch);

		System.out.println("Spells");
		spells.forEach(x -> {
			System.out.println(x);
		});
	}
	
	public static void init() {
		var ddragon = DDragonAPI.getInstance();
		gamePatch = ddragon.getVersions().get(0);
		
		LOG.info("Fetching spells...");
		var url = String.format("https://ddragon.leagueoflegends.com/cdn/%s/data/en_US/summoner.json", gamePatch);
		var body = Utils.fetch(url);
		loadSpells(body);
	}

	public static void loadSpells(String json) {
		DdragonSummonerResponse res = gson.fromJson(json, DdragonSummonerResponse.class);
		spells = res.data
				.values()
				.stream()
				.map(dSumm -> {
					var summ = new LightSpell();
					summ.name = dSumm.name;
					summ.cooldown = dSumm.cooldown[0];
					summ.id = Integer.parseInt(dSumm.key);
					summ.codename = dSumm.id;
					return summ;
				}).collect(Collectors.toList());
	}

	public static List<LightSpell> getSpells() {
		return spells;
	}
}

/**
 * http://ddragon.leagueoflegends.com/cdn/13.19.1/data/en_US/summoner.json
 */
class DdragonSummonerResponse {
	public String type = "summoner";
	public String version = "13.19.1";
	public Map<String, SummEntry> data;
	
	static class SummEntry {
		/**
		 * Code name.
		 */
		public String id;
		/**
		 * id (number) as string
		 */
		public String key;
		public String name;
		/**
		 * The {@code cooldown} array contains one value, which is the actual cooldown in seconds.
		 */
		double[] cooldown;
	}		
}
