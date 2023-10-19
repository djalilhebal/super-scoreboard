# Input forwarding options

Things I've considered or tried...


## Approach: Punched pane

Pure Java.

**Variant 1:**
- KScoreboard's elements do not completely cover the in-game UI.
- When you click on KScoreboard, it generates a synthetic click event (using `Robot`) that affects the ingame UI.
Like, KSpell receivees a click at `Point(x, t)`, so the app generates a click at `Point(x + OFFSET_X, y + OFFSET_Y)` which should be on the in-game Spell button.
- Issues:
    * Not sure how the system handles double clicks.
    * Events like long pressing and dragding will be messed up.
    * ...

**Variant 2:**
- KScoreboard's elements cover the in-game UI.
- When you click on KScoreboard:
    1. The clicked element is notified and clears the clicked point (using `Graphics.setComposite(SRC)` to make it transparent?).
    2. Then, we generate a synthetic at the same location, which should reach the underlying window.
    3. Finally, we restore the clicked element's normal state (unpunched)
- Issues:
    * Similar to the first variant.
    * Probably we will be a slave to how fast the UI can repaint (even with paint immediately and invoke and wait)
    *  ...


## Approach: Capture native events and generate Swing/AWT events.

This is how it should work: \
Listen to global events, synthesize Swing/AWT events, and dispatch them to the Swing framework and let it handle it or to specific components that should've been affected (like, if we click on the screen, the component that's directly under the mouse should be triggred).

May use
- JNativeHook
    * https://github.com/kwhat/jnativehook/
    * https://central.sonatype.com/artifact/com.github.kwhat/jnativehook/overview

Idea 1:
- Capture OS events.
    * Using JNativeHook https://github.com/kwhat/jnativehook
- Generate low level AWT/Swing events.
    * For example, using JNativeHook's `SwingMouseAdapter`
- Let Swing find the correct component to dispatch the event to it? Doesn't work. `event.source` is set by the OS/native toolkit impl.

Idea 1.2:
    * Dispatch them to the correct component by setting the event `source` then "firing" the event via
    `EventQueue#postEvent` or `Component#dispatchEvent`.

Workaround for Idea 1.2:
- Whenever a mouse event is read, get the component the mouse is hovering using `SwingUtilities#getDeepestComponentAt`.
It may not be pretty or super low latency, but we can generate mouse press, click, and move events. \
Mouse release, enter, and leave are more complex to mimic. \
Imagine this case: You press a button, move the mouse away, then release it. The button must receive both mouse press and mouse release events.

### Name

Forward OS input events to Swing components, as in Electron's `setIgnoreMouseEvents(true, {forward: true})`

- Swing Shadow
- Swing Event Synthesizer
- Swing Happy Synthesizer (Vocaloid)
- Swing Samsa (Vocaloid)
- Swing Nitrogen (invisible/gas/air + gen; similar to Electron)
- Swing Both Ways (both ways = OS and Swing)
- Event Forwarder
- Re:Dispatcher (a la Re:Zero and ReLIFE)


## Approach: Send native input messages (events) to the window and let it handle them.

Similar to the prev approach, but let's simplify the middleman and let Swing handle native events.

If they (Electron) could do it, so can we.

Something like:
- Display a normal window.
- Tell the system to make it transparent, making it ignore input events.
- Tell the system to inform us about all input (keyboard and mouse) events.
- Send the input events to the window, effectively making it react to input events.
