package ow.server;

import java.awt.Point;

import ow.common.ShipType;

public class Ship extends Entity {

  public final ShipType type;

  public Ship(ShipType type, Point initialLocation) {
    this.type = type;

    setLocation(initialLocation);
  }

}
