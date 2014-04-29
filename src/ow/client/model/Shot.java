package ow.client.model;

import ow.common.OMath;

public class Shot {

  public final int id;

  public final double startX, startY;
  public double x, y, rotation;
  public double velocity, maxDistance;

  public Shot(int id, double x, double y, double rotation, double velocity, double maxDistance) {
    this.id = id;
    this.x = startX = x;
    this.y = startY = y;
    this.rotation = rotation;
    this.velocity = velocity;
    this.maxDistance = maxDistance;
  }
  
  public void tick(int delta) {
    x += (float) (Math.cos(rotation) * delta * velocity / 1000);
    y -= (float) (Math.sin(rotation) * delta * velocity / 1000);
  }

  public boolean hasExpired() {
    return OMath.distanceSquared(startX, x, startY, y) >= maxDistance * maxDistance;
  }

}
