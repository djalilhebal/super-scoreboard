package me.djalil.scoreboard;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.sun.jna.Pointer;
import com.sun.jna.Native;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import me.djalil.scoreboard.components.KColor;

// TODO: Clean up and refactor. It's like an "experiment"
public class OverlayUtils extends JFrame implements KeyListener {

	public static void main(String[] args) {
		var app = new OverlayUtils();
		setWindowTransparent(app);
		EventSynthesizer.forwardKeyboardEvents(app);
		setForwardMouseEvents(app);
	}

	public OverlayUtils() {
		super();

		this.setVisible(false);
		this.setUndecorated(true);
		this.getRootPane().setOpaque(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setFocusableWindowState(false);
		this.setFocusable(false);
		this.setAlwaysOnTop(true);

		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("[OverlaidFrame] mouseClicked " + e);
			}

		});

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize);
		this.setBackground(KColor.withAlpha(KColor.PURPLE, 100));

		var btn = new JButton(new AbstractAction("Click me to exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});
		btn.setBorder(new LineBorder(Color.YELLOW, 2));
		btn.setSize(150, 50);
		btn.setBackground(new Color(255, 255, 255, 100));
		btn.setLocation(screenSize.width / 2 - btn.getWidth(), 0);
		this.setLayout(null);
		this.add(btn);

		var btn2 = new JButton("Click me");
		btn2.setLocation(0, 0);
		btn2.setSize(150, 50);
		this.add(btn2);

		this.setVisible(true);
	}

	// ---

	static void setWindowTransparent(Component w) {
		var componentHwnd = getHWnd(w);
		int originalExstyle = User32.INSTANCE.GetWindowLong(componentHwnd, WinUser.GWL_EXSTYLE);
		int transparentExstyle = originalExstyle | (WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT);
		User32.INSTANCE.SetWindowLong(componentHwnd, WinUser.GWL_EXSTYLE, transparentExstyle);
	}

	private static WinDef.HWND getHWnd(Component w) {
		WinDef.HWND hwnd = new WinDef.HWND();
		hwnd.setPointer(Native.getComponentPointer(w));
		return hwnd;
	}

	// === HOOKS ===

	private static WinUser.LowLevelMouseProc mouseHook;

	//public static Component c;

	private static Thread hookThread;
	
	static void setForwardMouseEvents(Component w) {
		var componentHwnd = getHWnd(w);

		mouseHook = new WinUser.LowLevelMouseProc() {

			@Override
			public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.MSLLHOOKSTRUCT lParam) {				
				if (nCode < 0) {
					// TODO
				}

				/*
				System.out.println("mouse hook");
				System.out.println("nCode\n " + nCode);
				System.out.println("wParam\n " + wParam);
				System.out.println("lParam\n " + lParam);
				System.out.println();
				*/

				var newLParam = makeLParam(lParam.pt.x, lParam.pt.y);
				User32.INSTANCE.PostMessage(componentHwnd, wParam.intValue(), wParam, newLParam);
				
				/*
				var s2cPointNative = new WinDef.POINT(lParam.pt.x, lParam.pt.y);
				var ok = MyUser32.INSTANCE.ScreenToClient(componentHwnd, s2cPointNative);
				System.out.println("Converted successfully? " + ok);
				
				var s2cPoint = new Point(s2cPointNative.x, s2cPointNative.y);
	
				Point appPoint = MouseInfo.getPointerInfo().getLocation(); 
				Point llhPoint = new Point(lParam.pt.x, lParam.pt.y); 
				Point cptPoint = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(cptPoint, c);
				System.out.println("llhPoint " + llhPoint);
				System.out.println("s2cPoint " + s2cPoint);
				System.out.println("appPoint " + appPoint);
				System.out.println("cptPoint " + cptPoint);
				System.out.println();
				*/

				return User32.INSTANCE.CallNextHookEx(null, nCode, wParam, newLParam);
				}
		};

		User32.HHOOK hhk = User32.INSTANCE.SetWindowsHookEx(User32.WH_MOUSE_LL, mouseHook,
				Kernel32.INSTANCE.GetModuleHandle(null), 0);
		var hooked = hhk != null;
		if (hooked) {
			System.out.println("Mouse hooked");
		} else {
			System.err.println("Failed to hook mouse events");
		}
		
		var msg = new WinUser.MSG();
		User32.INSTANCE.GetMessage(msg, new WinDef.HWND(Pointer.NULL), 0, 0);
		System.out.println(msg);
		//System.exit(0);
	}

	/**
	 * MAKELPARAM
	 * 
	 * See:
	 * - MAKELPARAM macro (winuser.h) https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-makelparam
	 * - Is there something like MAKELPARAM in Java / JNA? - Stack Overflow https://stackoverflow.com/a/65284768
	 * 
	 * @param low x
	 * @param high y
	 */
	private static WinDef.LPARAM makeLParam(int low, int high) {
		// to work for negative numbers
		int val =  (high << 16) | ((low << 16) >>> 16);
		var lparam = new WinDef.LPARAM(val);
		return lparam;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		System.out.println("[OverlaidFrame] keyTyped " + e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println("[OverlaidFrame] keyPressed " + e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		System.out.println("[OverlaidFrame] keyReleased " + e);
	}

}
