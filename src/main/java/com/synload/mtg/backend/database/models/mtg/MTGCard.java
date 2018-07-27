package com.synload.mtg.backend.database.models.mtg;


import com.synload.mtg.backend.api.utils.MultipleAPIRequest;
import com.synload.mtg.backend.database.models.Card;

import com.synload.mtg.backend.api.scryfall.*;
import com.synload.mtg.backend.api.utils.*;
import java.util.List;
import java.util.Map;

@MultipleAPIRequest({
    @APIRequest(url="https://api.scryfall.com/cards/{id}", key="id"),
    @APIRequest(url="https://api.scryfall.com/cards/multiverse/{multiverse}", key="multiverse[]"),
    @APIRequest(url="https://api.scryfall.com/cards/{set.code}/{number}", key="set.code,number")
})
public class MTGCard extends Card {

    @APIClassMapping(value="set", clazz=ScryFallHandler.class, method="getSet")
    public MTGSet set = null;

    @APIMapping({"name"})
    public String name;

    @APIMapping({"collector_number"})
    public String number;

    @APIMapping({"lang"})
    public String lang;

    @APIMapping({"type_line"})
    public String type;

    @APIMapping({"mana_cost"})
    public String mana;

    @APIMapping({"multiverse_ids"})
    public List<Integer> multiverse;

    @APIMapping({"id"})
    public String id;

    @APIMapping({"power"})
    public int power;

    @APIMapping({"toughness"})
    public int toughness;

    @APIMapping({"legalities"})
    public Map<String, String> legalities;

    @APIMapping({"image_uris"})
    public Map<String, String> images;

    @APIMapping({"rarity"})
    public String rarity;

    @APIMapping({"foil"})
    public boolean foil;

    @APIMapping({"nonfoil"})
    public boolean nonfoil;

    @APIMapping({"color_identity"})
    public String[] color_identity;

    @APIMapping({"colors"})
    public String[] colors;

    @APIClassMapping(value="card_faces", clazz=ScryFallHandler.class, method="setFace")
    public MTGCardFace[] faces;

    public MTGCard(){

    }

}
