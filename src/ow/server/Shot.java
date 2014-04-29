package ow.server;

import ow.common.OMath;

public class Shot extends Entity {

  public double startX, startY;
  public double x, y, rotation;
  public double velocity = 700;
  public double maxDistance = 800;

  public Shot(double x, double y, double rotation) {
    this.x = startX = x;
    this.y = startY = y;
    this.rotation = rotation;
  }

  public void tick(double millis) {
    x += (float) (Math.cos(rotation) * millis * velocity / 1000);
    y -= (float) (Math.sin(rotation) * millis * velocity / 1000);
  }

  public boolean hasExpired() {
    return OMath.distanceSquared(startX, x, startY, y) >= maxDistance * maxDistance;
  }

}
