package com.synload.mtg.backend.api.scryfall;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.synload.mtg.backend.api.utils.APIHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ScryFallHandler extends APIHandler {
  private static final Logger logger = LogManager.getLogger(ScryFallHandler.class.getName());
  public ScryFallHandler(){

  }
  public MTGCard getCardById(String id) {
    logger.debug("Fetched by id: " + id);
    long start = System.currentTimeMillis();
    MTGCard card = new MTGCard();
    card.id = id;
    card = (MTGCard) get(card);
    logger.info("Took " + (System.currentTimeMillis() - start) + " milliseconds to get data");
    return card;
  }
  public MTGCard getCardBySetId(String set, String number) {
    logger.info("Fetched by set and number: " + set + " " + number);
    long start = System.currentTimeMillis();
    MTGCard card = new MTGCard();
    card.number = number;
    card.set = set;
    card = (MTGCard) get(card);
    logger.info("Took " + (System.currentTimeMillis() - start) + " milliseconds to get data");
    return card;
  }
  public MTGSet getSetByCode(String set){
    logger.info("Fetched by set: " + set);
    long start = System.currentTimeMillis();
    MTGSet mset = new MTGSet();
    mset.code = set;
    mset = (MTGSet) get(mset);
    logger.info("Took " + (System.currentTimeMillis() - start) + " milliseconds to get data");
    return mset;
  }
}
