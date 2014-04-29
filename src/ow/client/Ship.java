package ow.client;

import ow.common.ShipType;


public class Ship {

  public final int id;
  public double x, y;
  public double targetX, targetY;
  public double rotation = Math.PI / 2;

  public final ShipType type;
  private final double maxSpeed;

  public Ship(int id, ShipType type) {
    this.id = id;
    this.type = type;
    this.maxSpeed = type.getMaxSpeed();
  }

  public Ship rotateToTarget() {
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

  public Ship halt() {
    targetX = x;
    targetY = y;
    return this;
  }

  public void tick(int delta) {
    if (type == ShipType.STATION) {
      rotation += 2 * Math.PI * delta / 1000 / 100;
    }

    moveTowardsTarget(delta);
  }

  private void moveTowardsTarget(int delta) {
    float dx = (float) (Math.cos(rotation) * delta * maxSpeed / 1000);
    float dy = -(float) (Math.sin(rotation) * delta * maxSpeed / 1000);

    if (Math.abs(x - targetX) <= Math.abs(dx) && Math.abs(y - targetY) <= Math.abs(dy)) {
      x = targetX;
      y = targetY;
    } else {
      x += dx;
      y += dy;
    }
  }

}
