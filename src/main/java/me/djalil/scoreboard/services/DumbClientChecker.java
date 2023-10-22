package me.djalil.scoreboard.services;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import me.djalil.scoreboard.model.LightGame;
import me.djalil.scoreboard.model.LightGameUtils;

/**
 * Dumb game checker. It just works.
 * 
 * - Pull-based.
 * 
 * - Accepts only one event listener (consumer).
 * 
 * Use like:
 * <pre>{@code
 * var dumbChecker = new DumbClientChecker();
 * dumbChecker.onNewGame = g -> System.out.println("Do something with the new game: " + g);
 * dumbChecker.start();
 * }</pre>
 */
public class DumbClientChecker extends Thread {
	private static final Logger LOG = Logger.getLogger(DumbClientChecker.class.getName());
	
	public static void main(String[] args) {
		var checker = new DumbClientChecker();
		checker.start();
		try {
			checker.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Consumer<LightGame> onNewGame = (x) -> System.out.println("[DumbClientChecker][default consumer] New game: " + x);

	private static final int normalSleep = 1;
	private static final int noGameSleep = 10;
	private static final int yesGameSleep = 20;
	
	LightGame currentGame;
	
	@Override
	public void run() {
		try {
			loop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
		
	private void fireCurrentGame() {
		onNewGame.accept(currentGame);
	}
	
	private void loop() throws InterruptedException {
		var lcdService = new LiveClientDataService();
		var lcuService = new LcuService();
		var opggService = new OpggService();
		
		while (!this.isInterrupted()) {
			var lcdGame = lcdService.getLiveGame();
			
			LOG.finer("lcdGame: " + lcdGame);
			if (lcdGame == null && currentGame != null) {
				currentGame = null;
				fireCurrentGame();
			} else if (lcdGame != null && currentGame == null) {
				// TODO: Should check that the session is InProgress
				var lcuGame = lcuService.getLiveGame();
				LOG.finer("lcuGame: " + lcuGame);
				if (lcuGame != null) {
					var newGame = new LightGame();
					newGame.merge(lcdGame);
					newGame.merge(lcuGame);

					var shouldFetchRunes = LightGameUtils.anyHasInspiration(newGame);
					LOG.finer("shouldFetchRunes? " + shouldFetchRunes);
					if (shouldFetchRunes) {
						var principalInfo = lcuService.getPrincipal();
						if (principalInfo != null) {
							LOG.finer("principal " + principalInfo);
							var region = principalInfo.get(0);
							var summonerName = principalInfo.get(1);
							var withRunes = opggService.getLiveGame(region, summonerName);
							LOG.finer("withRunes: " + withRunes);
							if (withRunes != null) {
								newGame.merge(withRunes);
							}
						}
					}
					
					currentGame = newGame;
					fireCurrentGame();
				}
			}
			
			if (lcdGame == null) {
				TimeUnit.SECONDS.sleep(noGameSleep);
			} else {
				TimeUnit.SECONDS.sleep(yesGameSleep);
			}
			TimeUnit.SECONDS.sleep(normalSleep);
		}
		
	}
}
