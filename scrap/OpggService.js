/**
 * Get live game.
 *
 * @remarks
 * - Maybe add a `game.meta.opggId` field.
 *
 * @public
 */
async function getLightLiveGame(region, summonerName) {
    const data = await getLiveGameData(region, summonerName);

    const queueId = data.queue_info.id;
    
    const participants = data.participants.map((p, i) => {

        return {
            id: i,
            summonerName: p.summoner.name,
            position: p.position,
            teamColor: p.team_key,
            championId: p.champion_id,
            runeTrees: [p.rune_build.primary_page_id, p.rune_build.secondary_page_id],
            runes: [...p.rune_build.primary_rune_ids, ...p.rune_build.secondary_rune_ids],
            spells: p.spells,
        }
    });

    const normalizedData = {
        queueId,
        participants,
    }

    return normalizedData;
}

async function getLiveGameData(region, summonerName) {
    const encryptedSummonerId = await getSummonerId(region, summonerName);
    const liveGameUrl = `https://op.gg/api/v1.0/internal/bypass/spectates/${region}/${encryptedSummonerId}?hl=en_US`;
    const result = await fetch(liveGameUrl).then(res => res.json());
    const data = result.data;
    return data;
}

/**
 * @private
 * @type {Map<string, string>}
 */
const summonerIdCache = new Map();

async function getSummonerIdCached(region, summonerName) {
    // TODO: Maybe add `.toLowerCase()`?
    const key = region + '/' + summonerName;
    if (cache.has(key)) {
        return cache.get(key);
    } else {
        try {
            const result = await getSummonerId(region, summonerName);
            cache.set(key, result);
            return result;
        } catch (e) {
            console.error(e);
            return null;
        }
    }
}

/**
 * Get the OPGG id of a specific summmoner.
 *
 * @example
 * getSummonerId("NA", "eisuke");
 * getSummonerId("oce", "incursio");
 * getSummonerId("oce", "Incursio");
 *
 * @remarks
 * - It extracts it from the summoner's page.
 * - We could use the autocompletion REST API.
 * - `region` and `summonerName` are not case sensitive.
 *   This is the current behavior of OP.GG.
 *   Other websites (like LeagueOfGraphs?) require `region` to be in all lowercase.
 */
async function getSummonerId(region, summonerName) {
    /*
    e.g contents of `https://www.op.gg/summoners/na/eisuke`
    ```html
    <script id="__NEXT_DATA__" type="application/json">{"props":{"pageProps":{"error":null,"region":"na","data":{"id":72239447,"summoner_id":"kS2MAl_hyw4gdOQehoEltjPcfyyjBW50sE2_DPV1AEYQfO0",
    ```
    */
    const finderRegex = /__NEXT_DATA__.*"summoner_id":"(?<encryptedSummonerId>[^"]+?)"/;
    
    const url = `https://www.op.gg/summoners/${region}/${summonerName}`
    const html = await fetch(url).then(res => res.text());
    const {encryptedSummonerId} = html.match(finderRegex).groups;
    return encryptedSummonerId;
}

// TESTING
(async function main() {
    const data = await getLightLiveGame('oce', 'incursio');
    console.log(data);
})();
