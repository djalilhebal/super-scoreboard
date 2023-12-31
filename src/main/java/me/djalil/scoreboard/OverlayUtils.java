package me.djalil.scoreboard;

import java.awt.Component;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

// TODO: Rename to KShellUtilities
public class OverlayUtils {

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

		hookThread = new Thread(() -> {
			User32.HHOOK hhk = User32.INSTANCE.SetWindowsHookEx(User32.WH_MOUSE_LL, mouseHook,
					Kernel32.INSTANCE.GetModuleHandle(null), 0);
			var hooked = hhk != null;
			if (hooked) {
				System.out.println("Mouse hooked");
			} else {
				System.err.println("Failed to hook mouse events");
			}
			
			// XXX: Why do we need to call GetMessage for the hook to start working?
			// What does this even mean?
			// "Retrieves a message from the calling thread's message queue.
			// The function dispatches incoming sent messages until a posted message is available for retrieval."
			// -- https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-getmessage
			var msg = new WinUser.MSG();
			User32.INSTANCE.GetMessage(msg, new WinDef.HWND(Pointer.NULL), 0, 0);
			System.out.println(msg);
		});
		hookThread.setName("MouseHookThread");
		hookThread.setDaemon(true);
		hookThread.start();
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

}
