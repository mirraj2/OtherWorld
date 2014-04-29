package ow.server;

import java.awt.Point;

import ow.common.ShipType;

public class Ship extends Entity {

  public final ShipType type;
  public boolean moving = false;
  public double rotation = Math.PI / 2;

  public Ship(ShipType type, Point initialLocation) {
    this.type = type;

    setLocation(initialLocation);
  }

}
