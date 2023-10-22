package me.djalil.scoreboard.components;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URI;

import javax.swing.JOptionPane;

import me.djalil.scoreboard.model.AppModel;

import static me.djalil.scoreboard.App.appHomePage;
import static me.djalil.scoreboard.App.appName;

/**
 * TODO/WIP.
 * 
 * See [How to Use the System Tray (The Java™ Tutorials > Creating a GUI With Swing > Using Other Swing Features)](https://docs.oracle.com/javase/tutorial/uiswing/misc/systemtray.html)
 */
public class KTray {
    
    private final static URI homeUri = URI.create(appHomePage);

    public static void main(String[] args) throws InterruptedException {
		var kTray = new KTray();
		kTray.installTray();
		Thread.currentThread().join();
	}

    void handleQuit() {
        System.exit(0);
    }
    
    void handleAbout() {
        //Desktop.isDesktopSupported()
        //desktop.isSupported(Desktop.Action.BROWSE)
        try {
            var desktop = Desktop.getDesktop();
            desktop.browse(homeUri);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    void installTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("SystemTray is not supported");
            return;
        }

        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(getIcon(), appName);
        final SystemTray tray = SystemTray.getSystemTray();
        
        MenuItem aboutItem = new MenuItem("About");
        onAction(aboutItem, this::handleAbout);
        popup.add(aboutItem);

        // Cosmic
        CheckboxMenuItem assumeItem = new CheckboxMenuItem("May assume Cosmic");
        assumeItem.setState(true);
        assumeItem.setEnabled(false);
        popup.add(assumeItem);

        // Delay
        // TODO: Add action handler
        //String str = JOptionPane.showInputDialog("Set recording delay to (integer)", AppModel.DEFAULT_RECORDING_DELAY);
        //int newDelay = Integer.parseInt(str);
        MenuItem delayItem = new MenuItem(String.format("Recording delay is %d s", AppModel.DEFAULT_RECORDING_DELAY));
        delayItem.setEnabled(false);
        popup.add(delayItem);
        
        // TODO: Separator?

        MenuItem quitItem = new MenuItem("Quit");
        onAction(quitItem, this::handleQuit);
        popup.add(quitItem);

        trayIcon.setPopupMenu(popup);
       
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
        }
    }
   
    /**
     * Utility method.
     * Adds `simpleAction` as an `ActionListener` to the `item`.
     * 
     * - Maybe replace `Runnable` with a more appropriate functional interface.
     * `Callable` and `Consumer` aren't good either:
	 * We don't want to force `simpleAction` to return anything or accept any argument.
     */
    static void onAction(MenuItem item, Runnable simpleAction) {
    	item.addActionListener(new ActionListener() {
    		
			@Override
			public void actionPerformed(ActionEvent e) {
				simpleAction.run();
			}
    		
    	});
    }
    
    /**
     * Get (or create) the tray icon.
     * 
     * - TODO: Draw a simple hexagon or crossed-square hexagon (https://commons.wikimedia.org/wiki/File:Crossed-square_hexagon.png).
     */
    public static Image getIcon() {
        var img = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
        var g2 = img.createGraphics();
        g2.setColor(KColor.PURPLE);
        g2.fillRect(0, 0, 250, 250);
        
        g2.setColor(KColor.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        g2.drawString("SS", 0, 12);

        g2.dispose();
        return img;
    }

}
