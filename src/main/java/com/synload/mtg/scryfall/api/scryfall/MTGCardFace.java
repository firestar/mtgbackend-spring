package com.synload.mtg.scryfall.api.scryfall;

import com.synload.mtg.scryfall.api.utils.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MTGCardFace implements Serializable {
  @APIMapping({"name"})
  public String name;

  @APIMapping({"type_line"})
  public String type;

  @APIMapping({"oracle_text"})
  public String oracle;

  @APIMapping({"flavor_text"})
  public String flavor;

  @APIMapping({"colors"})
  public List<String> colors;

  @APIMapping({"power"})
  public String power;

  @APIMapping({"toughness"})
  public String toughness;

  @APIMapping({"image_uris"})
  public Map<String, String> images;

  @APIMapping({"mana_cost"})
  public String mana;
}
