package me.djalil.scoreboard;

import me.djalil.scoreboard.model.LightGame;
import me.djalil.scoreboard.model.SpellTiming;

public interface IAppModel {
    LightGame getGame();
    
    void recordSpellUsage(String summonerName, int spellIndex);
    void clearSpellUsage(String summonerName, int spellIndex);
    SpellTiming getSpellUsage(String summonerName, int spellIndex);

    void setRecordingDelay(int delay);
	int getRecordingDelay();
}
