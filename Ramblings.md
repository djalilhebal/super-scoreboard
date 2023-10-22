# Ramblings


**Timer formats**:
- "`~SSS`" or "`~MM:SS`" When the summ will be up (in-game time).
- "`=SSS`" The summ's calculated cooldown at the moment of recording.
- "`<SSS`" How many seconds are left.

**Features**:
- [x] Able to change the recording delay.
- [ ] (WIP) Able to change the timer format.
- [ ] (WIP) Able to change whether we can assume Insp = Cosmic.


## Choices

- **Why no Electron?** \
    * Electron or CEF (Chromium Embedded Framework) are too heavy, at least for my poor machine.
    * Also, Electron forwards only `mousemove` events ("mouseenter" and "mouseleave").
    We are interested in other input events (e.g. mouse and key presses). \
    If we need to create the behavior we want, we need to use native system calls and hooks.
    So, why not use a more native-y language/framework like Java/Swing/JNA or C#/WPF or even C/ImGui?

- **Why Java + JNA?** \
Honestly, a C#/WPF app would be better:
Smaller in size, better mental model (feels more like HTML/React), and easier access to system APIs.
I am just more comfortable with Java.

- **Why Java Swing?** \
Was hoping to convert it to a standalone app using GraalVM's `native-image` or whatever.


## Other possibilities/features

These two require being able to programamtically type ingame.

- [ ] **Mimic pinging teammates** (their portrait, ult, and items)
    * Champion: "{championName} - Alive" and "{championName} - Respawning in {deathTimer}"
    * Ult: "{championName} - Ult?"
    * Item: "{championName} - {itemName}?"

- [ ] **Quick All Chat wheel**\*, you right click a portrait and it shows you a set of quick replies.
\*Similar to the Quick Emote or Quick Ping wheels: A radial popup menu shows up when right click a champ's portrait.
    * "Nice cannon, {championName}"


## Syncing timers between teammates

Using a free public MQTT broker (e.g. Eclipse's or HiveMQ's).

**MQTT topic**:
We could use `{gameId}-blue` and `{gameId}-red`, simple but not good enough since anyone can access and modify this info.
We need something more secure and specific to each game/team. \
The topic can be the lobby's chat session key, obvio.


## Typing in the Game Client

For example, sending flash timers to the team chat.

This won't work:
- [ANSWER: Type a String using java.awt.Robot - Stack Overflow](https://stackoverflow.com/a/29665705)
    * KAITO: `getSystemClipboard()` then put your content. The user can either `Control + V`, or we can simulate it using `Robot`'s key press and key release methods.

Apparently, the Game Client uses its own clipboard system,
so we can't simply write to the system clipboard and let the user <kbd>Control</kbd>+<kbd>V</kbd>.
```java
// SEE https://gist.github.com/felipegodoyf/d6ba01a8452d2b88d4fe5344eeb50101
// KAITO: Add to App or Utils
public void typeToLeagueChat(String str) {
    // Hit {ENTER}
    // Type str
    // Hit {ENTER}
}
```


## Questions / Problems

- [ ] Do all League Game Clients have the same aspect ratio?
    * KAITO: Probably not.
    We should support the (automatic) resizing and (manual) repositioning of the Super Scoreboard.
