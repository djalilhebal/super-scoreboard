package me.djalil.scoreboard.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import me.djalil.scoreboard.components.KParticipant;

/**
 * A LightGame partially ("lightly") represents a League game. Information may be missing.
 * (KAITO: Should be renamed to <b>VolatileGame</b> or <b>PartialGame</b> or something.)
 * 
 * <p>
 * In SuperScoreboard, a LightGame is an ever evolving (in-progress) live game.
 * While in MatchMatcher, LightGames won't change and should contain additional info like: winning team, timeline, stats, etc.  
 */
public class LightGame extends Thing {

	public long gameId;

	public List<Participant> participants;
	
	public String phase;

	/**
	 * Merge into this game.
	 * @param other
	 */
	public void merge(LightGame other) {
		LightGameUtils.merge(this, other);
	}
	
	// ---
	
	/**
	 * Order of participants as they should be shown on the scoreboard.
	 * {@code [...blueTeam.map(getSummonerName), ...redBlue.map(getSummonerName)]}
	 *
	 * It should be set only by the {@link LiveClientDataService}.
	 */
	public List<String> participantsOrder;

	public List<String> getParticipantsOrder() {
		return participantsOrder;
	}

	public void setParticipantsOrder(List<String> participantsOrder) {
	    if (!participantsOrder.equals(this.participantsOrder)) {
	        var oldVal = this.participantsOrder;
	        this.participantsOrder = participantsOrder;
	        fireChange("participantsOrder", oldVal, this.participantsOrder);
	    }
	}

	// ---
	
	/**
	 * Total game duration (if it's a finished game) or the current game time (it's in-progress).
	 */
	public double duration;

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		var oldVal = this.duration;
		this.duration = duration;
		fireChange("duration", oldVal, this.duration);
	}

	// ---
	
	public Participant getParticipantByChampion(int championId) {
		return findOne(p -> p.championId == championId);
	}

	public Participant getParticipantBySummoner(String summonerName) {
		return findOne(p -> p.summonerName.equals(summonerName));
	}

	public List<Participant> getBlueParticipants() {
		return findMany(p -> p.team.equals("BLUE"));
	}

	public List<Participant> getRedParticipants() {
		return findMany(p -> p.team.equals("RED"));
	}

	/**
	 * Returns the first matching element or null.
	 */
	private Participant findOne(Predicate<Participant> pred) {
		return participants.stream().filter(pred).findFirst().orElse(null);
	}

	/**
	 * Returns a new List containing all matching elements. May be empty.
	 */
	private List<Participant> findMany(Predicate<Participant> pred) {
		return participants.stream().filter(pred).collect(Collectors.toList());
	}
	

	@Override
	public String toString() {
		return String.format("Game(id=%d, participants=%s)", gameId, participants.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}

	public static class Participant {
		
		public String summonerName;
		
		/**
		 * "BLUE" or "RED"
		 */
		public String team;

		public int championId;
		
		public List<Integer> spellIds;
		
		/**
		 * Keystone, primary tree, secondary tree.
		 */
		public List<Integer> mainRuneIds;

		/**
		 * All rune ids.
		 * Should be ordered: primary - secondary - stats.
		 */
		public List<Integer> runeIds;
		
		/**
		 * Current owned items.
		 */
		public List<Integer> itemIds;
		
		@Override
		public String toString() {
			return String.format("Participant(%s, spells=%s, runes=%s, mainRunes=%s, items=%s)",
					summonerName, ""+spellIds, ""+runeIds, ""+mainRuneIds, ""+itemIds);
		}
	}
}
