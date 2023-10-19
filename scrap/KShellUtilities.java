/**
 * Named a la `SwingUtilities` and Electron `shell`.
 * It should work on `Window`.
 *
 * - Examples of {@link java.awt.Window} include {@link JFrame} and {@link JWindow}.
 * 
 * - [ ] Check `aott-desktop-client-core-1.0.339-sources.jar`:
 *  * `MouseServiceWinImpl`
 *  * `KeyboardServiceWinImpl`
 */
public class KShellUtilities {

    public static void setIgnoreEvents(Window w, boolean forward) {
        setIgnoreEvents(w);
        setForwardMouseMessages(w, forward);
        setForwardKeyboardMesages(w, forward);
    }
    
    /**
     * Mimics Electron's {@code setIgnoreMouseEvents(ignore=true, forward=true)}
     *
     * @see {@link com.sun.jna.platform.win32.WinUser}
     */
     // https://github.com/electron/electron/blob/a39c0ee659ce8f58bf6e7f8eb3c1c15fe5614143/shell/browser/native_window_views.cc#L1202
    static public void setIgnoreMouseEvents(Window win) {
        // See https://learn.microsoft.com/en-us/windows/win32/winmsg/extended-window-styles
        // See "Layered Windows" section on https://learn.microsoft.com/en-us/windows/win32/winmsg/window-features#layered-windows

        //ex_style |= (WS_EX_TRANSPARENT | WS_EX_LAYERED)
    }
    
    // https://github.com/electron/electron/blob/a39c0ee659ce8f58bf6e7f8eb3c1c15fe5614143/shell/browser/native_window_views_win.cc#L469
    static public void setForwardMouseMessages(Window win, boolean forward) {
    }

    static public void setForwardKeyboardMesages(Window win, boolean forward) {
    }

}
