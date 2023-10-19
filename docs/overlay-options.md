# Overlay options

Before we start:
- AFAIK, League's **Game Client** uses DirectX 9 on Windows and OpenGL on MacOS.
- By "real fullscreen," I mean exclusive fullscreen mode.
- By "fake fullscreen," I mean the borderless fullscreen mode (League option) or pressing F11 in Chrome/Chromium.
    * https://src.chromium.org/viewvc/chrome/trunk/src/ui/views/win/fullscreen_handler.cc?revision=HEAD&view=markup
    * https://stackoverflow.com/questions/2382464/win32-full-screen-and-hiding-taskbar


## How other projects "behave"

### Mobalytics
Mobalytics' overlay says "Press and hold 'tab' to interact". \
We can recreate this behavior as follows:
- Displaying a normal window.
- Storing the window's style (say `normalWinStyle`).
- Setting and storing the window's style after making it transparent (say `transparentWinStyle`).
- Changing the interactivity of the window means switching between `normalWinStyle` and `transparentWinStyle`.

That's how the MercuryTrade overlay also works.
See:
[`AbstractAdrFrame`](https://github.com/Exslims/MercuryTrade/blob/07bd6c341ef6593866e9031c102ce0176898cb54/app-ui/src/main/java/com/mercury/platform/ui/adr/components/AbstractAdrFrame.java#L68C1-L68C1)


## Overlay on top of a fullscreen app?

`win.setAlwaysOnTop(true)` and `win.toFront()` are not enough.

Apparently, this approach does not work on real fullscreen.

- "How to overlay application over other fullscreen applications?"
> This is impossible to do, exclusive fullscreen mode is exactly that exclusive it will only show one window nothing more, the only way to display anything "over it" is to draw on it by hooking the api that renders the game usually on EndScene() in direct3d games
>
> -- [NoG5's comment](https://www.reddit.com/r/software/comments/9wols7/comment/e9mzyg9/?utm_source=share&utm_medium=web2x&context=3)

---

- [R3nzSkin](https://github.com/R3nzTheCodeGOD/R3nzSkin)'s window is displayed in-game.
Apparently, it works as NoG5 described.
Check [`end_scene` in `R3nzSkin/Hooks.cpp`](https://github.com/R3nzTheCodeGOD/R3nzSkin/blob/f42bb66c19381ef4e066ed8001055008692dc3c4/R3nzSkin/Hooks.cpp#L306)

Pseudocode:
```js
Hooks.install(gameClientProcess, "SceneEnd", (d3Device) -> {
    let gui = new Imgui_d3_impl(d3Device);
    gui.render();
});
```


### What do popular overlays use?

Apparently, FACEIT and OP.GG use **gelectron** (now called **goverlay**)

- goverlay https://github.com/hiitiger/goverlay
- FACEIT / gelectron fork https://github.com/faceit/gelectron
- another gelectron fork https://github.com/honzapatCZ/gelectron
- OP.GG / resources / `app.asar` / `package.json`:
```jsonc
{
  //...
  "optionalDependencies": {
    "electron-overlay": "file:./gelectron/electron-overlay",
    "node-ovhook": "file:./gelectron/node-ovhook",
    "rust-process": "file:./rust-process"
  },
  //...
}
```

### Testing goverlay

- [List of Direct3D 9 games | PCGamingWiki](https://www.pcgamingwiki.com/wiki/List_of_Direct3D_9_games)

- **League of Legends** and **Resident Evil 4** use Direct3D 9

- [x] Install Resident Evil 4

- [x] Clone https://github.com/hiitiger/goverlay

- [ ] Build and test
    * KAITO: It won't compile.


## Conclusion

If I were to recreate it, I would use JavaScript and gelectron.

Or C++ and Dear Imgui a la R3nzSkin.

Or create my own framework similar to **goverlay**-- **joverlay**.

Or even just forking Electron to make it forward all mouse events (not just "mouse move").
