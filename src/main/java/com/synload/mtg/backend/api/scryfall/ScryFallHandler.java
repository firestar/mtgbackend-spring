package com.synload.mtg.backend.api.scryfall;


import com.synload.mtg.backend.api.utils.APIHandler;
import com.synload.mtg.backend.database.models.mtg.MTGCard;

public class ScryFallHandler extends APIHandler {

    public ScryFallHandler(){

    }
    public MTGCard getCardById(String id) {
        MTGCard card = new MTGCard();
        card.id = id;
        this.get(card);
        return null;
    }

}
