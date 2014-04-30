package ow.server;

import java.awt.Point;
import java.util.Collection;

import ow.common.Faction;
import ow.common.ShipType;

public class Ship extends Entity {

  public final ShipType type;
  public final Faction faction;

  public boolean moving = false;
  public double rotation = Math.PI / 2;
  public final double maxSpeed;
  public final Collection<Point> gunLocations;

  public Ship(Faction faction, ShipType type, Point location) {
    this(faction, type, location.x, location.y);
  }

  public Ship(Faction faction, ShipType type, double x, double y) {
    this.faction = faction;
    this.type = type;
    this.x = x;
    this.y = y;
    this.maxSpeed = type.getMaxSpeed();
    this.gunLocations = type.getGunLocations();
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

  public Ship rotation(double rotation) {
    this.rotation = rotation;
    return this;
  }

  public Ship moving(boolean moving) {
    this.moving = moving;
    return this;
  }

}
