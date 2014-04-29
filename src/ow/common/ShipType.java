package ow.common;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public enum ShipType {

  MINI, STATION;

  public double getMaxSpeed() {
    return maxSpeeds.get(this);
  }

  public String getImageName() {
    StringBuilder sb = new StringBuilder();
    sb.append(name().toLowerCase());
    sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    sb.append(".png");
    return sb.toString();
  }

  private static final Map<ShipType, Double> maxSpeeds = ImmutableMap.<ShipType, Double>builder()
      .put(MINI, 200d)
      .put(STATION, 1d)
      .build();

}
