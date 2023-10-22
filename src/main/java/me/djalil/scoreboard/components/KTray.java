package me.djalil.scoreboard.components;

import static me.djalil.scoreboard.App.appHomePage;
import static me.djalil.scoreboard.App.appName;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

import javax.swing.JOptionPane;

import me.djalil.scoreboard.model.AppModel;
import me.djalil.scoreboard.model.SpellUtils;

/**
 * Add tray icon.
 * 
 * See [How to Use the System Tray (The Java™ Tutorials > Creating a GUI With
 * Swing > Using Other Swing
 * Features)](https://docs.oracle.com/javase/tutorial/uiswing/misc/systemtray.html)
 */
public class KTray {

	private final static URI homeUri = URI.create(appHomePage);

	public static void main(String[] args) throws InterruptedException {
		var kTray = new KTray(new AppModel());
		kTray.installTray();
		Thread.currentThread().join();
	}

	private AppModel appModel;

	public KTray(AppModel appModel) {
		this.appModel = appModel;
	}

	void handleQuit() {
		System.exit(0);
	}

	void handleAbout() {
		if (!Desktop.isDesktopSupported()) {
			return;
		}

		var desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Desktop.Action.BROWSE)) {
			return;
		}

		try {
			desktop.browse(homeUri);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void installTray() {
		if (!SystemTray.isSupported()) {
			System.err.println("SystemTray is not supported");
			return;
		}

		final TrayIcon trayIcon = new TrayIcon(getIcon(), appName);
		final SystemTray tray = SystemTray.getSystemTray();

		MenuItem aboutItem = new MenuItem("About");
		aboutItem.addActionListener(_ev -> this.handleAbout());

		// Cosmic
		CheckboxMenuItem assumeItem = new CheckboxMenuItem("May assume Cosmic");
		assumeItem.setState(SpellUtils.assumeCosmic);
		// For now, let's make it read-only.
		assumeItem.setEnabled(false);

		// Timer template
		MenuItem timerItem = new Menu("Timer template: Remaining");
		// For now, let's make it read-only.
		timerItem.setEnabled(false);

		// Delay
		MenuItem delayItem = new MenuItem(String.format("Recording delay is %ds", appModel.getRecordingDelay()));
		delayItem.addActionListener(_ev -> {
			try {
				String str = JOptionPane.showInputDialog("Set recording delay to (positive integer)", appModel.getRecordingDelay());
				int newDelay = Integer.parseUnsignedInt(str);
				appModel.setRecordingDelay(newDelay);
				// FIXME: Not DRY enough!
				delayItem.setLabel(String.format("Recording delay is %ds", appModel.getRecordingDelay()));
			} catch (NumberFormatException ex) {
				//ex.printStackTrace();
				// Do nothing.
			}
		});

		MenuItem quitItem = new MenuItem("Quit");
		quitItem.addActionListener(_ev -> this.handleQuit());

		PopupMenu popup = new PopupMenu();
		popup.add(aboutItem);
		popup.addSeparator();
		popup.add(assumeItem);
		popup.add(timerItem);
		popup.add(delayItem);
		popup.addSeparator();
		popup.add(quitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.err.println("TrayIcon could not be added.");
		}
	}

	/**
	 * Get (or create) the tray icon.
	 * 
	 * - TODO: Draw a simple hexagon or crossed-square hexagon
	 * (https://commons.wikimedia.org/wiki/File:Crossed-square_hexagon.png).
	 */
	public static Image getIcon() {
		var img = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
		var g2 = img.createGraphics();
		g2.setColor(KColor.INDIGO);
		g2.fillRect(0, 0, 250, 250);

		g2.setColor(KColor.WHITE);
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
		g2.drawString("SS", 0, 12);

		g2.dispose();
		return img;
	}

}
