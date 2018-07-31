package com.synload.mtg.backend.api.scryfall;


import com.synload.mtg.backend.api.utils.MultipleAPIRequest;
import com.synload.mtg.backend.api.utils.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@MultipleAPIRequest({
  @APIRequest(url="https://api.scryfall.com/cards/{id}", key="id", cache=-1),
  @APIRequest(url="https://api.scryfall.com/cards/multiverse/{multiverse}", key="multiverse", cache=-1),
  @APIRequest(url="https://api.scryfall.com/cards/{set}/{number}", key="set,number", cache=-1)
})
@APILink(from = "set", to="code", clazz = MTGSet.class, field="cards")
@APIPriority(2)
public class MTGCard implements Serializable {
  @APIRequestMapping(value={"set"}, there="code")
  public transient MTGSet setData = null;

  @APIMapping(value = {"set"}, loadingVar = true)
  public String set;

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
  public String power;

  @APIMapping({"toughness"})
  public String toughness;

  @APIMapping({"legalities"})
  public Map<String, String> legalities;

  @APIMapping({"image_uris"})
  public Map<String, String> images;

  @APIMapping({"rarity"})
  public String rarity;

  @APIMapping({"foil"})
  public Boolean foil;

  @APIMapping({"nonfoil"})
  public Boolean nonfoil;

  @APIMapping({"color_identity"})
  public List<String> color_identity;

  @APIMapping({"colors"})
  public List<String> colors;

  @APIClassMapping({"card_faces"})
  public List<MTGCardFace> faces;

  public MTGCard(){

  }

}
