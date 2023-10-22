package me.djalil.scoreboard;

public class App {

	public final static String appName = "SuperScoreboard";
	public final static String appVersion = "0.0.1";
    public final static String appHomePage = "https://github.com/djalilhebal/super-scoreboard/";

	/**
	 * TODO/refactor: Make it instantiate and wire the model, view, and controller.
     * Currently AppController kinda does everything. 
	 */
	public static void main(String[] args) {
		AppController.main(args);
	}

}
