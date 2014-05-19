package ow.client.model;

import ow.common.Faction;
import ow.common.OMath;
import ow.common.ShipType;

public class Ship {

  public final int id;
  public double x, y;
  public double rotation;
  public boolean moving = false;
  public double hp;

  public final Faction faction;
  public final ShipType type;
  public final double maxSpeed, maxHP;

  public Ship(int id, Faction faction, ShipType type, double hp, double maxHP) {
    this.id = id;
    this.faction = faction;
    this.type = type;
    this.maxSpeed = type.getMaxSpeed();
    this.hp = hp;
    this.maxHP = maxHP;
  }

  public Ship rotateToTarget(double targetX, double targetY) {
    rotation = OMath.getTargetRotation(x, y, targetX, targetY);
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
