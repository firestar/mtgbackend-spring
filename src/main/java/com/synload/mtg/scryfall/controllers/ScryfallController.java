package com.synload.mtg.scryfall.controllers;

import com.synload.mtg.scryfall.api.scryfall.MTGSet;
import com.synload.mtg.scryfall.api.scryfall.ScryFallHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class ScryfallController {

  @Autowired
  ScryFallHandler scryFallHandler;

  @GetMapping("/card/{id}")
  public Object getCardById(@PathVariable("id") String id){
    return scryFallHandler.getCardById(id);
  }

  @GetMapping("/card/{set}/{number}")
  public Object getCardBySetAndNumber(@PathVariable("set") String set, @PathVariable("number") String number){
    return scryFallHandler.getCardBySetId(set, number);
  }

  @GetMapping("/set/{set}")
  public Object getSetByCode(@PathVariable("set") String set){
    return scryFallHandler.getSetByCode(set);
  }

  @GetMapping("/cards/{set}")
  public Object getCardsBySet(@PathVariable("set") String set){
    MTGSet setData = scryFallHandler.getSetByCode(set);
    if(setData!=null){
      return setData.cards;
    }
    return null;
  }

}
