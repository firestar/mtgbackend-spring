package com.synload.mtg.backend.database.models;

import java.util.List;
import java.util.Map;

public class MTGCard extends Card {

    private MTGCard foil = null;
    private MTGSet set = null;
    private String lang;
    private String type;
    private String mana;
    private List<Integer> multiverse;
    private String id;
    private int power;
    private int toughness;
    private Map<String, String> legalities;

    public MTGCard(String name, MTGCard foil, MTGSet set, String lang, String type, String mana, List<Integer> multiverse, String id) {
        super(name);
        this.foil = foil;
        this.set = set;
        this.lang = lang;
        this.type = type;
        this.mana = mana;
        this.multiverse = multiverse;
        this.id = id;
    }
}
