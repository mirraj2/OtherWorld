package ow.server;

import java.awt.Point;
import java.util.Collection;

import ow.common.ShipType;

public class Ship extends Entity {

  public final ShipType type;
  public boolean moving = false;
  public double rotation = Math.PI / 2;
  public final double maxSpeed;
  public final Collection<Point> gunLocations;

  public Ship(ShipType type, Point initialLocation) {
    this.type = type;
    this.maxSpeed = type.getMaxSpeed();
    this.gunLocations = type.getGunLocations();

    setLocation(initialLocation);
  }

  public void tick(double millis) {
    if (moving) {
      moveForward(millis);
    }
  }

  private void moveForward(double millis) {
    x += (float) (Math.cos(rotation) * millis * maxSpeed / 1000);
    y -= (float) (Math.sin(rotation) * millis * maxSpeed / 1000);
  }

}
