package ow.common;

import java.awt.Point;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public enum ShipType {

  MINI, STATION;

  public double getMaxSpeed() {
    return maxSpeeds.get(this);
  }

  public Collection<Point> getGunLocations() {
    return gunLocations.get(this);
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
          .put(STATION, 1d)
          .build();

  private static final Multimap<ShipType, Point> gunLocations =
      ImmutableMultimap.<ShipType, Point>builder()
          .put(MINI, new Point(11, 0))
          .build();

}
