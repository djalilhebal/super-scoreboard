package me.djalil.scoreboard.model;

import java.time.Instant;

//TODO: Where should it be kept?
// 		on LightGame, Participant, AppModel, or even keep it in KSpell?
public class SpellTiming {
	public static SpellTiming EMPTY;
	static {
		EMPTY = new SpellTiming();
		EMPTY.cooldown = 100;
		EMPTY.usedAtIngame = 0;
		EMPTY.usedAt = Instant.EPOCH;
	}
	
	//public String summonerName;
    //public int spellId;
    public double cooldown;
    public double usedAtIngame;
    public Instant usedAt;

    public long getRemainingSeconds() {
    	long secondsElapsed = Instant.now().getEpochSecond() - usedAt.getEpochSecond();
    	long remaining = (long) (cooldown - secondsElapsed);
    	return remaining;
    }
    
    /**
     * @returns 1 if it's up or {@code ]0; 1[} if it's on cooldown.
     */
    public double getElapsedRatio() {
    	long secondsElapsed = Instant.now().getEpochSecond() - usedAt.getEpochSecond();
    	if (secondsElapsed > cooldown) {
    		return 1;
    	} else {
        	double ratio = secondsElapsed / cooldown;
	    	return ratio;    		
    	}
    }
    
    /**
     * In seconds.
     */
    public int getWhenUpIngame() {
    	int whenUpIngame = (int)(usedAtIngame + cooldown);
    	return whenUpIngame;
    }
    
    @Override
    public String toString() {
    	if (this == EMPTY) {
    		return "SpellTiming(EMPTY)";
    	}
    	return String.format("SpellTiming(cooldown=%s, usedAtInGame=%s, usedAt=%s)",
    			""+cooldown, ""+usedAtIngame, ""+usedAt);
    }

	public boolean isOnCooldown() {
		return getElapsedRatio() == 1;
	}
}
