package ow.common;

import java.util.Map;

import com.google.common.collect.Maps;
import org.newdawn.slick.Color;

public enum Faction {

  EXPLORERS, FEDERATION;

  public Color getColor() {
    return factionColors.get(this);
  }

  private static final Map<Faction, Color> factionColors = Maps.newEnumMap(Faction.class);
  static {
    factionColors.put(EXPLORERS, new Color(1f, 1f, 1f));
    factionColors.put(FEDERATION, new Color(1f, .1f, .1f));
  }

}
