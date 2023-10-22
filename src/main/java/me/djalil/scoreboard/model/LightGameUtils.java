package me.djalil.scoreboard.model;

import java.util.Objects;
import java.util.stream.Collectors;

public class LightGameUtils {

	/**
	 * Any participant has inspiration?
	 */
	public static boolean anyHasInspiration(LightGame game) {
		try {
			return game != null &&
					game.participants != null &&
					game.participants.stream().allMatch(SpellUtils::hasInspiration);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * For every field that isn't 0 or null, copy its value from source to target.
	 * Effectively doing something like:
	 * 
	 * <pre>{@code
	 * target.gameId = source.gameId;
	 * target.participants.get(0).spells = target.participants.get(0).spells;
	 * }</pre>
	 *
	 * The target must contain a list of participants with names.
	 */
	public static void merge(LightGame target, LightGame source) {
		// Sanity checks
		Objects.requireNonNull(target, "target must not be null");
		Objects.requireNonNull(source, "source must not be null");
		
		// in case the target is an empty (newly instantiated) game.
		if (target.participants == null) {
			target.participants = source.participants;
		}
		
		var allSummonerNamesSet = target.participants.stream().allMatch(p -> p.summonerName != null);
		if (!allSummonerNamesSet) {
			throw new IllegalArgumentException("participants' summonerNames must be set");
		}
		
		// id
		if (source.gameId != 0) {
			target.gameId = source.gameId;
		}

		// game time
		if (source.duration != 0) {
			synchronized (target) {
				target.setDuration(source.duration);
			}
		}

		// participant team, runes, and items
		source.participants.forEach(p -> {
			var targetP = target.getParticipantBySummoner(p.summonerName);
			if (targetP == null) {
				System.out.printf("target.getParticipantBySummoner(%s) returned %s", p.summonerName, targetP);
				System.out.println("target: " + target);
				System.out.println("source: " + source);
				System.exit(1);
			}
			
			if (p.team != null) {
				targetP.team = p.team;
			}
			
			if (p.championId != 0) {
				targetP.championId = p.championId;
			}
			
			if (p.spellIds != null) {
				targetP.spellIds = p.spellIds;
			} 

			if (p.mainRuneIds != null) {
				targetP.mainRuneIds = p.mainRuneIds;
			}

			if (p.runeIds != null) {
				targetP.runeIds = p.runeIds;
			}
			
			if (p.itemIds != null) {
				targetP.itemIds = p.itemIds;
			}			
		});
		
		if (source.participantsOrder != null) {
			target.setParticipantsOrder(source.participantsOrder);
		}

	}

	/**
	 * WIP. UNUSED!
	 * 
	 * We assume that two instances represent the same game if they share the same
	 * participants. This is probably a wrong assumption for example if you are
	 * playing custom games 5v5 or some tournament where two participants.
	 * 
	 * @returns probably the same game?
	 */
	public static boolean sameGame(LightGame a, LightGame b) {
		if (a.gameId != 0 && b.gameId != 0) {
			return a.gameId == b.gameId;
		} else {
			return uniqueStr(a).equals(uniqueStr(b));
		}
	}

	private static String uniqueStr(LightGame g) {
		String blue = g.participants.stream().filter(p -> p.team.equals("BLUE")).map(p -> p.summonerName).sorted()
				.collect(Collectors.joining(","));

		String red = g.participants.stream().filter(p -> p.team.equals("RED")).map(p -> p.summonerName).sorted()
				.collect(Collectors.joining(","));

		String str = String.format("%s vs %s", blue, red);
		return str;
	}

}
