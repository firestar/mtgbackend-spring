package com.synload.mtg.scryfall.api.scryfall;

import com.synload.mtg.scryfall.api.utils.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@APIRequest(url="https://api.scryfall.com/sets/{code}", key="code", cache=-1)
@APIPriority(1)
public class MTGSet implements Serializable{
  @APIMapping({"name"})
  public String name;

  @APIMapping({"code"})
  public String code;

  @APIMapping({"set_type"})
  public String type;

  @APIMapping({"card_count"})
  public Integer count;

  @APIMapping({"released_at"})
  public String release;

  @APIMapping({"icon_svg_uri"})
  public String icon;

  @APIClear
  public List<MTGCard> cards = new ArrayList<>();

  public MTGSet() {
  }
}
