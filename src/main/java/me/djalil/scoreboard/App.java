package me.djalil.scoreboard;

import me.djalil.scoreboard.components.KTray;
import me.djalil.scoreboard.model.AppModel;

public class App {

	public final static String appName = "SuperScoreboard";
	public final static String appVersion = "0.0.1";
    public final static String appHomePage = "https://github.com/djalilhebal/super-scoreboard/";

	/**
	 * TODO/refactor: Currently AppController kinda does everything. 
	 */
	public static void main(String[] args) {
		var appModel = new AppModel();
		setAppModel(appModel);

		var appController = new AppController();
		appController.setAppModel(appModel);
		appController.createAndShowUI();

		var tray = new KTray(appModel);
		tray.installTray();
	}
	
	// ---
	
	private static AppModel appModel;

	@Deprecated
	public static AppModel getAppModel() {
		return appModel;
	}
	
	public static void setAppModel(AppModel appModel) {
		App.appModel = appModel;
	}
	

}
