# TODO

List of things we can improves, but won't.


## Logic

- [ ] Get rid of `LcuService`.
The only info we are extracting from LCU Gameflow Session is `gameId`, which we aren't using, really.
We can get the rest from **LiveClientData** (mainly spell codenames and champion codenames). \
Although, we can still use it to make the app more push-y instead of pull-y.


## Perf

- [ ] In local data sources (e.g. `LiveClientDataService`), consider making timeouts (connection and request for example) super small since we are working locally anyways.

- [ ] To detect when the **Game Client** is visible (meaning: running and in the foreground), we can use a pull-based approach using WinAPI hooks:
    - [SetWindowsHookExA](https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setwindowshookexa)

        * `WH_SHELL` and [ShellProc callback function - Win32 apps | Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/winmsg/shellproc)
        
        * `WH_CBT` hook and [CBTProc callback function (Windows) - Win32 apps | Microsoft Learn](https://learn.microsoft.com/en-us/windows/win32/winmsg/cbtproc)



## UI/Swing

- [ ] Create (or find a component/layout), `KSection`, that behaves like CSS `box-sizing: border-box;`.
    * Makes the GUI easier to program.

- [x] Resize the text to fit a container

- [x] Place the text in the center

- [ ] Java Swing: Draw text with outline (to make it easier to read.)
    * https://stackoverflow.com/questions/10016001/how-to-draw-an-outline-around-text-in-awt
    * https://docs.oracle.com/javase/8/docs/api/java/awt/font/TextLayout.html#draw-java.awt.Graphics2D-float-float-

- [ ] Handle the case where the scoreboard is mirrored.
    * Can we know whether the scoreboard is mirrored?
    Yes, by reading (or _watching_) the config file `game.cfg`:
    ```ini
    [HUD]
    MirroredScoreboard=0
    ```

- [ ] Make it work on diff screen resolutions.

- [ ] Make it work in fullscreen mode.
See [JOverlay.md](./docs/JOverlay.md)
