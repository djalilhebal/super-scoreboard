package me.djalil.scoreboard.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import me.djalil.scoreboard.AppController;
import me.djalil.scoreboard.IAppModel;
import me.djalil.scoreboard.Utils;
import me.djalil.scoreboard.services.DdragonService;
import me.djalil.scoreboard.services.DumbClientChecker;
import me.djalil.scoreboard.services.IGameProvider;
import me.djalil.scoreboard.services.LcuService;
import me.djalil.scoreboard.services.LiveClientDataService;
import me.djalil.scoreboard.services.OpggService;

public class AppModel extends Thing implements IAppModel {
	private static final Logger LOG = Logger.getLogger(AppModel.class.getName());

    private GameTimeTicker timeTicker;
    private DumbClientChecker clientChecker;

    public AppModel() {
    	initServices();

    	timeTicker = new GameTimeTicker();
    	timeTicker.start();
    	
    	clientChecker = new DumbClientChecker();
    	clientChecker.onNewGame = (g) -> {
    		System.out.println("[AppModel][DumbClientChecker] NewGame: " + g);
    		this.setGame(g);
    	};
    	clientChecker.start();
    }
    
    /**
     * Increment the current game time by `1` every second.
     */
    class GameTimeTicker extends Thread {
        
        public GameTimeTicker() {
            this.setName(GameTimeTicker.class.getName());
        }
        
        @Override
        public void run() {
            while (!this.isInterrupted()) {
                if (game != null/* && game.gamePhase.equals("InProgress")*/) {
                    synchronized(game) {
                        game.setDuration(game.getDuration() + 1);
                    }
                }
                try {
					Thread.sleep(1 * 1000);
				} catch (InterruptedException e) {}
            }
        }
    }

    // ---

    List<LightSpell> spells;

    LightSpell getSpellById(int spellId) {
		return spells.stream().filter(x -> x.id == spellId).findFirst().get();
	}

    // ---
    
    private LightGame game = null;
    
    public LightGame getGame() {
		return game;
	}

	public void setGame(LightGame game) {
		var oldVal = this.game;
		this.game = game;
		fireChange("game", oldVal, this.game);
	}

	// ---
	
	/**
	 * Is TAB being pressed?
	 */
	private boolean isTabbing;
		
	public boolean getIsTabbing() {
		return isTabbing;
	}

	public void setIsTabbing(boolean isTabbing) {
		var oldVal = this.isTabbing;
		this.isTabbing = isTabbing;
		LOG.finest("isTabbing " + isTabbing);
		fireChange("isTabbing", oldVal, this.isTabbing);		
	}
	
	// ---
	
    /**
     * We care if the game is running and visible/active/fullscreen/foreground.
     * - Maybe rename to gameVisible, gameClientActive, or gameForeground.
     */
    private boolean isGameRunning = true;

	public boolean getIsGameRunning() {
		return this.isGameRunning;
	}

	public void setIsGameRunning(boolean isGameRunning) {
		this.isGameRunning = isGameRunning;
	}

	// ---
	
	private LcuService lcuService;
	private LiveClientDataService liveClientDataService;
	private OpggService opggService;
	
	private void initServices() {
		lcuService = new LcuService();
		liveClientDataService = new LiveClientDataService();
		opggService = new OpggService();

		// Static data
    	var spellsFile = "ddragon-summoner.json";
    	var spellsBody = Utils.readText(spellsFile);
    	DdragonService.loadSpells(spellsBody);
    	//DdragonService.init();
    	spells = DdragonService.getSpells();

    	// Live data
		lcuService.onNewGame(newGame -> {
			LOG.info("On new game (from LcuService)");

			LOG.info("Fetching main runes...");
			var withMainRunes = liveClientDataService.getLiveGame();
			newGame.merge(withMainRunes);
			
			var shouldFetchRunes = LightGameUtils.anyHasInspiration(newGame);
			LOG.info("Should fetch minor runes? " + shouldFetchRunes);
			LOG.info(newGame.toString());
			if (shouldFetchRunes) {
				LOG.info("Fetching minor runes...");
				refreshPrincipal();
				var withRunes = opggService.getLiveGame(region, summonerName);
				newGame.merge(withRunes);
			}
			setGame(newGame);
		});
		
		// Disable the "smart"/push/websocket-based approach.
		//lcuService.init();
	}
	
	// ---

    /**
     * Principal's region, lower cased.
     * For example, "euw".
     */
    String region = null;
    
    /**
     * Principal's summoner name. Must preserve the letter case.
     */
    String summonerName = null;

	/**
	 * @return successfully updated
	 */
	public boolean refreshPrincipal() {
		var regionSummonerPair = lcuService.getPrincipal();
		
		if (regionSummonerPair != null) {
			this.region = regionSummonerPair.get(0);
			this.summonerName = regionSummonerPair.get(1);
			return true;
		}
		return false;
	}
	
    // ---
    
    /**
     * Update the current game time and participants' items and positions on the scoreboard.
     * @returns success
     */
    private boolean refreshGameDetails() {
    	var lcdGame = liveClientDataService.getLiveGame();
    	if (lcdGame != null) {
    		game.merge(lcdGame);
    		return true;
    	}
        return false;
    }

    /**
     * Fetches or update runes for all participants.
     *
     * <ul>Options:
     * <li>1. From OPGG.
     * <li>2. From Riot's Spectator API.
     * <li>3. From raw Spectator data? We can spectate our own game, no?
     * First fetch the first chunk and decode its metadata.
     *
     * @returns success
     */
    private boolean refreshRunes() {
    	refreshPrincipal();
    	
    	var opggGame = opggService.getLiveGame(region, summonerName);
    	if (opggGame != null) {
        	game.merge(opggGame);
            return true;
    	}
    	return false;
    }

    // ---
    
	public static int DEFAULT_RECORDING_DELAY = 5;

    /**
     *  How long do we usually take to record summ usage?
     *  In Seconds.
     */
    private int recordingDelay = DEFAULT_RECORDING_DELAY;
    
    /**
     * See {@link #recordingDelay}
     */
    @Override
    public int getRecordingDelay() {
    	return recordingDelay;
    }

    /**
     * See {@link #recordingDelay}
     */
    @Override
    public void setRecordingDelay(int newDelay) {
    	assert newDelay >= 0;
    	
    	recordingDelay = newDelay;
    }

    // ---
    
    private Map<String, SpellTiming> spellTimings = new HashMap<>();

    private String keyForSpellUsage(String summonerName, int spellIndex) {
    	return summonerName + "." + spellIndex;
    }
    
    @Override
	public void recordSpellUsage(String summonerName, int spellIndex) {
    	var key = keyForSpellUsage(summonerName, spellIndex);

    	// Update items and game time
    	refreshGameDetails();

    	var whenUsedIngame = game.getDuration() - recordingDelay;
    	var whenUsed = Instant.now().minusSeconds(recordingDelay);
    	var participant = game.getParticipantBySummoner(summonerName);

    	var spellId = participant.spellIds.get(spellIndex);
    	var spell = getSpellById(spellId);
    	var reducedCooldown = SpellUtils.getReducedCooldown(participant, spell);

    	var usage = new SpellTiming();
    	usage.usedAt = whenUsed;
    	usage.usedAtIngame = whenUsedIngame;
    	usage.cooldown = reducedCooldown;
    	
    	spellTimings.put(key, usage);
    	LOG.info(String.format("SpellTiming added:\n\t%s\n\t%s\n", ""+participant, ""+spell));
	}

	@Override
	public void clearSpellUsage(String summonerName, int spellIndex) {
    	var key = keyForSpellUsage(summonerName, spellIndex);
    	spellTimings.remove(key);
	}

	@Override
	public SpellTiming getSpellUsage(String summonerName, int spellIndex) {
    	var key = keyForSpellUsage(summonerName, spellIndex);
    	var ret = spellTimings.get(key);
    	if (ret == null) {
    		return SpellTiming.EMPTY;
    	} else {
    		return ret;
    	}
	}

}
