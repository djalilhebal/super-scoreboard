# Contents

## Static data

Stale data

- `ddragon-summoner.json`
    * Summoner spells.
    * Version 13.19.1.


## Test data

[The game on LeagueOfGraphs](https://www.leagueofgraphs.com/match/euw/6611407126#participant1).

`test-data--euw-6611407126/*`

- `lcuLiveClient--not-found.json`
    * "resource not found"
    * From the **LiveClientData** endpoint.
    * The game client is actually showing the loading screen.

- `lcuLiveClient--time-09.json`
    * From the **LiveClientData** endpoint.
    * Game time is 9 something.
    * In `allPlayers`, Quinn and Briar's positions are reversed on the scoreboard.
    Briar is index 0 while Quinn is index 1.

- `lcuLiveClient--time-10--swapped.json`
    * From the **LiveClientData** endpoint.
    * Game time is 10 something.
    * The principal (me, Quinn) swapped Briar and Quinn's positions via the in-game Scoreboard.
    Quinn is index 0 (top) and Briar is index 1 (jg).

- `lcuLiveClient--time-1261--has-lucids.json`
    * From the **LiveClientData** endpoint.
    * Game time is 1261 something.
    * Sylas has Lucids (`itemID` 3158, "Ionian Boots of Lucidity") in his `items`.

- `lcuSession--phase-InProgress.json`
    * LCU Gameflow Session, exists.

- ???
    * LCU Gameflow Session, not found.
    
- `opggAutocomplete.json`
    * OPGG summoners autocomplete response.
    * Contains one entry for `ExactlyOnce#euw`.

- `opggSession-not-found.json`
    * OPGG live game.
    * The summoner is not currently in-game, or we need to retry later.
    
- `opggSession.json`
    * OPGG live game.

---

END.
