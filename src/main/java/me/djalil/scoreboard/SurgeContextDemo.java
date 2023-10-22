package me.djalil.scoreboard;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

//import static scoreboard.Surge.useContext;

public class SurgeContextDemo {

	public static void main(String[] args) {
		var f = new JFrame();
		f.add(new MyPage());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(500, 500);
		f.setVisible(true);
	}
}

@FunctionalInterface
interface ThemeContext {
	String getTheme();
}

class MyPage extends JPanel implements ThemeContext {

	public MyPage() {
		add(new MyPanel());
	}

	// XXX: Change me!
	private String theme = "dark";

	public String getTheme() {
		return theme;
	}

}

class MyPanel extends JPanel {
		
	public MyPanel() {
		add(new MyButton("Click Me!"));
	}
}

class MyButton extends JButton implements SurgeUser {

	public MyButton(String text) {
		super();
		this.setText(text);
	}

	@Override
	public void addNotify() {
		super.addNotify();

		String theme = useContext(ThemeContext.class);
		this.setBackground("dark".equals(theme) ? Color.BLACK : Color.WHITE);
		this.setForeground(new Color(255, 107, 107)); // coral
	}

}
