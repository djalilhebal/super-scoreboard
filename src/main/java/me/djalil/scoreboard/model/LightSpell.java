package me.djalil.scoreboard.model;

public class LightSpell /*extends Thing*/ {
    public int id;
    
    /**
     * e.g. "SummonerFlash" or "SummonerCherryFlash" (both have the name "Flash")
     */
    public String codename;

    /**
     * e.g. "Flash".
     */
    public String name;
    
    /**
     * In seconds.
     * 
     * - Why double? For some reason `SummonerCherryFlash` has a cooldown of `0.25`.
     */
    public double cooldown;
    
    @Override
    public String toString() {
    	return String.format("Spell(id=%d, codename=%s, cooldown=%f)", id, codename, cooldown);
    }
}
