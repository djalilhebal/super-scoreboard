# SuperScoreboard: Ramblings

More random thoughts.

- It started as an overlay called Seer Stone (camp respawn tracker using "AI"/OpenCV).
I gave up on it... for now.
Then I had the idea to make something similar, simpler, but somewhat useful: Superimposed Scoreboard.

How it should work:
```java
while (true) {
    if (isPortOpen(LIVECLIENTDATA_PORT)) {
        var currentGame = AppModel.getGame();

        // with gameId and phase
        var newPartialGame = lcuSession.getLiveGame();
        if (shouldFireNewGame(currentGame, newPartialGame)) {
            var withMainRunes = lcdService.getLiveGame();
            //var withAllRunes = opggService.getLiveGame(region, participant);
            var newGame = newPartialGame.merge(withMainRunes);
            AppModel.setGame(newGame);
        }
    } else {
        AppModel.setGame(null);
    }

    sleep(GAME_CHECKER_INTERVAL);
}
```


## Manually reordering participants

- **Reordering players.**
For example, the player can can reorder players on the scoreboard based on their current roles.
This just needed in case Riot messes up the order of players (for no reason, which it sometimes does) or if players swap roles.

```java
/* partial */ class KParticipant {
    private static final List<String> ROLES = List.of(
        "TOP",
        "JUNGLE",
        "MIDDLE",
        "BOTTOM",
        "SUPPORT"
        );

    private void setupManualOrderingMenu() {
        var menu = new JPopupMenu();

        ROLES.forEach(role -> {
            final int newIndex = ROLES.indexOf(role);
            menu.add(new AbstractAction("Move to " + role) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    var kList = (KParticipantList)getParent();
                    kList.moveChildTo(newIndex, KParticipant.this);
                }
            });
        });

        this.setComponentPopupMenu(menu);
    }

}
```


## Java / Swing

### MVC or whatever

- PureMVC Java https://en.wikipedia.org/wiki/PureMVC

- [x] READ: [Java SE Application Design With MVC](https://www.oracle.com/technical-resources/articles/javase/mvc.html)
    * Visited 2023-09-03 `[archived:20231003134524]`

- [x] READ: [The MVC pattern and Swing - Stack Overflow](https://stackoverflow.com/a/17781394)
    * TLDR: Think of `Action` as `Controller`.

- [ ] REREAD: The MVC pattern and Swing - Stack Overflow https://stackoverflow.com/a/16850977
    * KAITO: More like MVP

- Long press event
    + [ ] Java Swing Tips: Long pressing the JButton to get a JPopupMenu
    https://java-swing-tips.blogspot.com/2014/03/long-pressing-jbutton-to-get-jpopupmenu.html
    + [ ] java - JButton long press event - Stack Overflow
    https://stackoverflow.com/questions/23872483/jbutton-long-press-event


## Interesting junk

- **Apache Commons Geometry** https://commons.apache.org/proper/commons-geometry
    * `Lines`: `segmentFromLocations` or `subsetFromInterval`

#interesting #Java
- https://github.com/HarmfulBreeze/overlay
    * Customizable UI for League of Legends champion select spectating.
    * Uses CEF.
    * Has useful classes: Process monitor and WS auto reconnector


#interesting #JNA
- `WinDef.HWND findWindow(final String windowClass, final Pattern titlePattern)`
https://github.com/mmarquee/ui-automation/blob/7f173ef93b391f663c3dd3597475832970786f8d/src/main/java/mmarquee/automation/utils/Utils.java#L339


#WinAPI #Java
- [ ] java - How Can i detect if full screen application is running in windows? - Stack Overflow
https://stackoverflow.com/a/60501359

### Geometry

- [geometry - Split line into multiple line - Stack Overflow](https://stackoverflow.com/questions/32663758/split-line-into-multiple-line)

- [geometry - Partition line into equal parts - Stack Overflow](https://stackoverflow.com/questions/3542402/partition-line-into-equal-parts)

---

END.
