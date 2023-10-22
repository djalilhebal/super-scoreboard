package me.djalil.scoreboard.services;

import java.util.List;

public class OpggAutocompleResponse {
    public List<Entry> data;

    public static class Entry {
        public String summoner_id;
        public String name;
    }

}
