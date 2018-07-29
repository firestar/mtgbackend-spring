package com.synload.mtg.backend.database.models.mtg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.synload.mtg.backend.api.utils.*;
import com.synload.mtg.backend.database.models.Card;
import com.synload.mtg.backend.database.models.Set;
import java.util.Date;

@APIRequest(url="https://api.scryfall.com/sets/{code}", key="code", cache=300000)
public class MTGSet extends Set {
  @APIMapping({"name"})
  public String name;

  @APIMapping({"code"})
  public String code;

  @APIMapping({"set_type"})
  public String type;

  @APIMapping({"card_count"})
  public int count;

  @APIMapping({"released_at"})
  public String release;

  @APIMapping({"icon_svg_uri"})
  public String icon;

  @JsonIgnore
  public Card[] cards;

  public MTGSet() {
  }
}
