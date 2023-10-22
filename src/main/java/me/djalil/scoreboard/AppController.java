package me.djalil.scoreboard;

import static me.djalil.scoreboard.Utils.readText;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import me.djalil.scoreboard.components.KColor;
import me.djalil.scoreboard.components.KScoreboard;
import me.djalil.scoreboard.model.AppModel;
import me.djalil.scoreboard.services.LcuService;
import me.djalil.scoreboard.services.LiveClientDataService;
import me.djalil.scoreboard.services.OpggService;

public class AppController {

	private static final Logger LOG = Logger.getLogger(AppController.class.getName());

	private static AppModel appModel;

	public static AppModel getAppModel() {
		return appModel;
	}

	public static void main(String[] args) {
		appModel = new AppModel();

		var screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// var win = new JWindow();
		var win = new JFrame();
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setUndecorated(true);
		win.getRootPane().setOpaque(false);
		win.setLocationRelativeTo(null);
		win.setLocation(0, 0);
		win.setSize(screenSize);
		win.setAlwaysOnTop(true);
		win.setOpacity(0.5f);
		win.setFocusable(false);
		win.setFocusableWindowState(false);
		win.setFocusTraversalKeysEnabled(false);
		win.setBackground(KColor.AIR);

		win.addWindowListener(new WindowAdapter() {
			public void windowDeactivated(WindowEvent e) {
				LOG.info("windowDeactivated");
				/*
				win.setAlwaysOnTop(true);
				win.toFront();
				*/
			}
		});
		
		var keyHandler = new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					getAppModel().setIsTabbing(true);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					getAppModel().setIsTabbing(false);
				}
			}

		};
		win.addKeyListener(keyHandler);

		var container = new JPanel();
		container.setOpaque(false);
		container.setBackground(KColor.AIR);
		container.setSize(screenSize);
		container.setLocation(0, 0);
		container.setLayout(new GridBagLayout());
		container.setAlignmentX(Container.CENTER_ALIGNMENT);
		container.setAlignmentY(Container.CENTER_ALIGNMENT);

		win.add(container);
		
		appModel.onChange("isTabbing", () -> {
		    var shouldShow = appModel.getIsTabbing() && appModel.getIsGameRunning();
		    win.setVisible(shouldShow);
		});

		appModel.onChange("game", () -> {
			SwingUtilities.invokeLater(() -> {
				LOG.info("Game changed. Removing old KScoreboard...");

				container.removeAll();
				
				var game = appModel.getGame();
				if (game != null) {
					LOG.info("Creating a new KScoreboard...");
					var kScoreboard = new KScoreboard(game);
					container.add(kScoreboard, new GridBagConstraints());
				}

				container.revalidate();
				container.repaint();
			});
		});

		appModel.onChange("isTabbing", () -> {
			SwingUtilities.invokeLater(() -> {
				container.setVisible(appModel.getIsTabbing());
			});
		});

		var menu = new JPopupMenu();
		menu.add(new AbstractAction("Refresh principal") {
			@Override
			public void actionPerformed(ActionEvent e) {
				appModel.refreshPrincipal();
			}
		});
		menu.add(new AbstractAction("Load initial game (major runes and items)") {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadInitialGame();
			}
		});
		menu.add(new AbstractAction("Load minor runes") {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadRunes();
			}
		});
		menu.add(new AbstractAction("Load state swapped positions") {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadSwappedGame();
			}
		});
		menu.add(new AbstractAction("Load state with items") {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadWithLucidsGame();
			}
		});
		menu.addSeparator();
		menu.add(new AbstractAction("Exit") {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		//container.setComponentPopupMenu(menu);
		//loadInitialGame();

		win.setVisible(true);

		LOG.info("Making the window transparent...");
		OverlayUtils.setWindowTransparent(win);

		EventSynthesizer.forwardKeyboardEvents(win);
		LOG.info("Forwarding mouse events...");
		OverlayUtils.setForwardMouseEvents(win);
		LOG.info("Forwarding mouse events: DONE.");
	}

	// --- TEST DATA ---
	
	static void loadInitialGame() {
		// init services
		var lcuService = new LcuService();
		var liveClientDataService = new LiveClientDataService();

		var lcuSessionBody = readText("lcuSession--phase-InProgress.json");
		var lcuLiveClientDataBody = readText("lcuLiveClient--time-09.json");
		// var lcuLiveClientDataBody2 = readText("lcuLiveClient-time-10-swapped.json");
		// var lcuLiveClientDataBody3 = readText("lcuLiveClient-time-1261-lucids.json");

		// var game = new LightGame();
		// game.merge( lcuService.getLiveGame(lcuSessionBody) );
		var game = lcuService.getLiveGame(lcuSessionBody);
		game.merge(liveClientDataService.getLiveGame(lcuLiveClientDataBody));

		/*
		 * LOG.info("game id " + game.gameId); LOG.info("game time " + game.duration);
		 * LOG.info("Kaito: " + game.getParticipantBySummoner("ExactlyOnce"));
		 * LOG.info("Light: " + game.getParticipantByChampion(115));
		 */
		getAppModel().setGame(game);
	}

	static void loadRunes() {
		var opggService = new OpggService();
		var opggSessionBody = readText("opggSession.json");
		var opggGame = opggService.getLiveGame(opggSessionBody);

		getAppModel().getGame().merge(opggGame);
	}
	
	static void loadSwappedGame() {
		var liveClientDataService = new LiveClientDataService();

		var game = getAppModel().getGame();
		var lcuLiveClientDataBody2 = readText("lcuLiveClient--time-10--swapped.json");
		game.merge(liveClientDataService.getLiveGame(lcuLiveClientDataBody2));
	}

	static void loadWithLucidsGame() {
		var liveClientDataService = new LiveClientDataService();

		var game = getAppModel().getGame();
		var lcuLiveClientDataBody3 = readText("lcuLiveClient--time-1261--has-lucids.json");
		game.merge(liveClientDataService.getLiveGame(lcuLiveClientDataBody3));
	}

}
