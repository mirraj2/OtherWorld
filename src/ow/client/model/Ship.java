package ow.client.model;

import ow.common.Faction;
import ow.common.ShipType;

public class Ship {

  public final int id;
  public double x, y;
  public double rotation;
  public boolean moving = false;

  public final Faction faction;
  public final ShipType type;
  public final double maxSpeed;

  public Ship(int id, Faction faction, ShipType type) {
    this.id = id;
    this.faction = faction;
    this.type = type;
    this.maxSpeed = type.getMaxSpeed();
  }

  public Ship rotateToTarget(double targetX, double targetY) {
    rotation = Math.atan2(targetX - x, targetY - y) - Math.PI / 2;
    if (rotation < 0) {
      rotation += Math.PI * 2;
    }
    return this;
  }

  public Ship setLocation(double x, double y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public Ship setRotation(double rotation) {
    this.rotation = rotation;
    return this;
  }

  public Ship moving(boolean moving) {
    this.moving = moving;
    return this;
  }

  public Ship halt() {
    moving = false;
    return this;
  }

  public void tick(int delta) {
    if (type == ShipType.STATION) {
      rotation += 2 * Math.PI * delta / 1000 / 100;
    }

    if (moving) {
      moveForward(delta);
    }
  }

  private void moveForward(int delta) {
    float dx = (float) (Math.cos(rotation) * delta * maxSpeed / 1000);
    float dy = -(float) (Math.sin(rotation) * delta * maxSpeed / 1000);

    x += dx;
    y += dy;
  }

}
