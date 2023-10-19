/**
 * - If you need maps, use {@link Collectors#toMap}.
 */
public interface IStaticDataProvider {

    List<LightSpell> getSpells();

    List<LightChampion> getChampions();
    
    // ---
    
    /**
     * @param json DDragon JSON.
     */
    List<LightSpell> getSpells(String json);
    
    /**
     * @param json DDragon JSON.
     */
    List<LightChampion> getChampions(String json);

}
