package com.synload.mtg.backend.database.models.mtg;

import com.synload.mtg.backend.api.utils.*;
import java.util.Map;

public class MTGCardFace {
    @APIMapping({"name"})
    public String name;

    @APIMapping({"type_line"})
    public String type;

    @APIMapping({"oracle_text"})
    public String oracle;

    @APIMapping({"flavor_text"})
    public String flavor;

    @APIMapping({"colors"})
    public String[] colors;

    @APIMapping({"power"})
    public int power;

    @APIMapping({"toughness"})
    public int toughness;

    @APIMapping({"image_uris"})
    public Map<String, String> images;

    @APIMapping({"mana_cost"})
    public String mana;
}
