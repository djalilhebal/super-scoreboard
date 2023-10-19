function normalizeLiveData(data) {
    // CHAOS is RED
    // ORDER is BLUE
    //teamColor = teamId == "CHAOS" ? "RED" : "BLUE";    
}

interface LightLiveGameData {
    queueId: number,
    
    // initial
    participants: Array<{
        id: number,
        
        position: string,
        
        summonerName: string,
        
        teamColor: 'BLUE' | 'RED',
        
        championId: number,

        // Rune pages or rune ids, [primary, secondary]
        runeTrees: Array<number>,

        /**
         * Ids of runes used (keystone and minor runes).
         * Live Game endpoint does not return runes.
         * primaries then secondaries
         */
        runes: Array<number> | null,
        
        /**
         * Ids of summoner spells.
         *
         * TODO: Maybe use tuple [number, number]
         */
        spells: Array<number>,
    }>
}
