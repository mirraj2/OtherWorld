package ow.common;

import java.awt.Point;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public enum ShipType {

  MINI, FIGHTER, COLONY_SHIP, STATION, CHEATSHIP;

  public double getMaxSpeed() {
    return maxSpeeds.get(this);
  }

  /**
   * Gets turn speed in radians per millisecond
   */
  public double getTurnSpeed() {
    double d = turnSpeeds.get(this);
    return d / 60000.0 * Math.PI * 2;
  }

  public Collection<Point> getGunLocations() {
    return gunLocations.get(this);
  }

  public double getHP() {
    return hitpoints.get(this);
  }

  public String getImageName() {
    StringBuilder sb = new StringBuilder();
    sb.append(name().toLowerCase());
    sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    sb.append(".png");
    return sb.toString();
  }

  private static final Map<ShipType, Double> maxSpeeds =
      ImmutableMap.<ShipType, Double>builder()
          .put(MINI, 200d)
          .put(FIGHTER, 150d)
          .put(COLONY_SHIP, 50d)
          .put(STATION, 1d)
          .put(CHEATSHIP, 2000d)
          .build();

  // in rotations per minute
  private static final Map<ShipType, Double> turnSpeeds =
      ImmutableMap.<ShipType, Double>builder()
          .put(MINI, 30d)
          .put(FIGHTER, 30d)
          .put(STATION, 30d)
          .put(COLONY_SHIP, 15d)
          .put(CHEATSHIP, 100d)
          .build();

  private static final Multimap<ShipType, Point> gunLocations =
      ImmutableMultimap.<ShipType, Point>builder()
          .put(MINI, new Point(11, 0))
          .put(FIGHTER, new Point(6, -13))
          .put(FIGHTER, new Point(6, 13))
          .put(CHEATSHIP, new Point(65, 0))
          .build();

  private static final Map<ShipType, Double> hitpoints =
      ImmutableMap.<ShipType, Double>builder()
          .put(MINI, 25d)
          .put(FIGHTER, 50d)
          .put(COLONY_SHIP, 250d)
          .put(STATION, 1000d)
          .put(CHEATSHIP, 999999d)
          .build();

}
