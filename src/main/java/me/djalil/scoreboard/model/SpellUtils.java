package me.djalil.scoreboard.model;

import java.util.Objects;

/**
 * See https://leagueoflegends.fandom.com/wiki/Haste#Summoner_spell_haste
 */
public class SpellUtils {

	/**
	 * If we don't have runes, should we assume they have Cosmic Insight if they have Inspiration?
	 */
	public static boolean assumeCosmic = true;

    /**
     * Total summoner spell haste the participant has at this moment.
     */
    public static int getTotalSummHaste(LightGame.Participant p) {
    	Objects.requireNonNull(p);
    	Objects.requireNonNull(p.mainRuneIds, "mainRuneIds must not be null");
    	Objects.requireNonNull(p.itemIds, "itemIds must not be null");
    	
        int total = 0;
        total += hasCosmic(p) ? SpellUtils.COSMIC_HASTE : 0;
        total += hasLucids(p) ? SpellUtils.LUCIDS_HASTE : 0;
        return total;
    }
    
    public static double getReducedCooldown(LightGame.Participant p, LightSpell spell) {
    	var totalSummHaste = getTotalSummHaste(p);
    	var originalCooldown = spell.cooldown;
    	var reducedCooldown = getReducedCooldown((long)originalCooldown, totalSummHaste);
    	return reducedCooldown;
    }

	/**
	 * 
	 * @param base  - base cooldown
	 * @param haste - total summoner spell haste
	 * @return
	 */
	static double getReducedCooldown(long base, long haste) {
		return base * (100 / (100.0 + haste));
	}

	static boolean hasLucids(LightGame.Participant p) {
		return p.itemIds.indexOf(LUCIDS_ITEM_ID) > -1;
	}
	
	static boolean hasCosmic(LightGame.Participant p) {
		if (p.runeIds != null) {
			return p.runeIds.indexOf(COSMIC_INSIGHT_RUNE_ID) > -1;
		} else {
			return hasInspiration(p) && assumeCosmic;
		}
	}

	static boolean hasInspiration(LightGame.Participant p) {
		return p != null &&
				p.mainRuneIds != null &&
				p.mainRuneIds.indexOf(INSPIRATION_RUNE_TREE_ID) > -1;
	}
	
	// ---
	
    final static int LUCIDS_ITEM_ID = 3158;
    
	final static int INSPIRATION_RUNE_TREE_ID = 8300;
	
	final static int COSMIC_INSIGHT_RUNE_ID = 8347;

	/**
	 *  Cosmic Insight
	 */
	static public final int COSMIC_HASTE = 18;

	/**
	 * Ionian Boots of Lucidity
	 */
	static public final int LUCIDS_HASTE = 12;

}
