package me.djalil.scoreboard.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.djalil.scoreboard.model.LightGame;

public class OpggLiveGameResponse {
	public OpggLiveGame data;

	public static class OpggLiveGame {
		public List<Participant> participants;

		public static class Participant {
			public Summoner summoner;
			/**
			 * "BLUE" or "RED"
			 */
			public String team_key;
			public int champion_id;
			public RuneBuild rune_build;
		}

		public static class Summoner {
			/**
			 * OP.GG's encryptedSummonerId.
			 */
			public String summoner_id;
			/**
			 * summonerName. e.g. "QuinnAD"
			 */
			public String name;
		}

		/*
		 * "rune_build": { "primary_page_id": 8000, "primary_rune_ids": [ 8005, 8009,
		 * 9103, 8017 ], "secondary_page_id": 8300, "secondary_rune_ids": [ 8345, 8347
		 * ], "stat_mod_ids": [ 5005, 5008, 5002 ] },
		 */
		public static class RuneBuild {
			public int primary_page_id;
			public List<Integer> primary_rune_ids;
			public int secondary_page_id;
			public List<Integer> secondary_rune_ids;
			public List<Integer> stat_mod_ids;
		}
	}
	
	public LightGame toLight() {
		var game = new LightGame();
		
		game.participants = this.data.participants.stream().map(opggP -> {
			var p = new LightGame.Participant();
			
			p.summonerName = opggP.summoner.name;
			
			p.team = opggP.team_key;
			
			p.championId = opggP.champion_id;

			var build = opggP.rune_build;
			p.runeIds = new ArrayList<>();
			p.runeIds.addAll(build.primary_rune_ids);
			p.runeIds.addAll(build.secondary_rune_ids);
			p.runeIds.addAll(build.stat_mod_ids);

			return p;
		}).collect(Collectors.toList());
		
		return game;
	}
}
