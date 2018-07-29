package com.synload.mtg.backend.api.scryfall;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.synload.mtg.backend.api.utils.APIHandler;
import com.synload.mtg.backend.database.models.mtg.MTGCard;
import com.synload.mtg.backend.database.models.mtg.MTGSet;

public class ScryFallHandler extends APIHandler {
  public ScryFallHandler(){

  }
  public MTGCard getCardById(String id) {
    MTGCard card = new MTGCard();
    card.id = id;
    get(card);
    return null;
  }
  public MTGCard getCardById(String set, String number) {
    MTGSet setObj = new MTGSet();
    setObj.code = set;
    setObj = (MTGSet) get(setObj);
    try {
      ObjectMapper om = new ObjectMapper();
      System.out.println(om.writeValueAsString(setObj));
    }catch( Exception e){
      e.printStackTrace();
    }
    MTGCard card = new MTGCard();
    card.number = number;
    card.set = setObj;
    get(card);
    return null;
  }
}
