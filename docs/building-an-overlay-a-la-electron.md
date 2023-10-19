# Building an overlay a la Electron

The idea is simple:

1. Create a normal window (the overlay window).

2. Tell the operating system to pretend that the window is invisible.
(You can click through it, but you can't interact with it because no mouse/keyboard events are sent.)

3. Tell the operating system to send you all mouse events.

4. Forward all mouse events to the overlay window.

Now, you effectively can interact with the invisible overlay _and_ the window underneath it (e.g. League) at the same time.

Similar to [Electron's `setIgnoreMouseEvents`][electron-setIgnoreMouseEvents] (with the option `forward` set to `true`).

[electron-setIgnoreMouseEvents]: https://www.electronjs.org/docs/latest/api/browser-window#winsetignoremouseeventsignore-options


## JNA

Just use JNA to implement the bare minimum to achieve the desired behavior.

...


## Relevant Electron snippets

<details>

From Electron's [`shell/browser/native_window_views_win.cc`](https://github.com/electron/electron/blob/b0590b6ee874fbeac49bb5615525d145835eb64f/shell/browser/native_window_views_win.cc#L556)
```cpp
void NativeWindowViews::SetForwardMouseMessages(bool forward) {
  if (forward && !forwarding_mouse_messages_) {
    forwarding_mouse_messages_ = true;
    forwarding_windows_.insert(this);

    // Subclassing is used to fix some issues when forwarding mouse messages;
    // see comments in |SubclassProc|.
    SetWindowSubclass(legacy_window_, SubclassProc, 1,
                      reinterpret_cast<DWORD_PTR>(this));

    if (!mouse_hook_) {
      mouse_hook_ = SetWindowsHookEx(WH_MOUSE_LL, MouseHookProc, nullptr, 0);
    }
  } else if (!forward && forwarding_mouse_messages_) {
    forwarding_mouse_messages_ = false;
    forwarding_windows_.erase(this);

    RemoveWindowSubclass(legacy_window_, SubclassProc, 1);

    if (forwarding_windows_.empty()) {
      UnhookWindowsHookEx(mouse_hook_);
      mouse_hook_ = nullptr;
    }
  }
}

// ...

LRESULT CALLBACK NativeWindowViews::MouseHookProc(int n_code,
                                                  WPARAM w_param,
                                                  LPARAM l_param) {
  if (n_code < 0) {
    return CallNextHookEx(nullptr, n_code, w_param, l_param);
  }

  // Post a WM_MOUSEMOVE message for those windows whose client area contains
  // the cursor since they are in a state where they would otherwise ignore all
  // mouse input.
  if (w_param == WM_MOUSEMOVE) {
    for (auto* window : forwarding_windows_) {
      // At first I considered enumerating windows to check whether the cursor
      // was directly above the window, but since nothing bad seems to happen
      // if we post the message even if some other window occludes it I have
      // just left it as is.
      RECT client_rect;
      GetClientRect(window->legacy_window_, &client_rect);
      POINT p = reinterpret_cast<MSLLHOOKSTRUCT*>(l_param)->pt;
      ScreenToClient(window->legacy_window_, &p);
      if (PtInRect(&client_rect, p)) {
        WPARAM w = 0;  // No virtual keys pressed for our purposes
        LPARAM l = MAKELPARAM(p.x, p.y);
        PostMessage(window->legacy_window_, WM_MOUSEMOVE, w, l);
      }
    }
  }

  return CallNextHookEx(nullptr, n_code, w_param, l_param);
}
```

</details>


## See also

- [x] [Java: Making a window click-through (including text/images) | Stack Overflow](https://stackoverflow.com/a/28772306)
    * TLDR: Use JNA to set the window's "GWL_EXSTYLE" to `WS_EX_LAYERED` and `WS_EX_TRANSPARENT`.

- [Hooks Overview - Win32 apps | Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/winmsg/about-hooks#wh_mouse_ll)
- [LowLevelMouseProc callback function - Win32 apps | Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/winmsg/lowlevelmouseproc)
- [LowLevelKeyboardProc callback function - Win32 apps | Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/winmsg/lowlevelkeyboardproc)
