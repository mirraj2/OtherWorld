package ow.server.model;

import java.awt.Point;

public class Entity {

  private static int ID_COUNTER = 0;

  public final int id = ID_COUNTER++;
  public double x, y;

  public void setLocation(Point p) {
    this.x = p.x;
    this.y = p.y;
  }

  public double distSquared(Entity b) {
    return distSquared(b.x, b.y);
  }

  public double distSquared(double xx, double yy) {
    double dx = xx - x;
    double dy = yy - y;

    return dx * dx + dy * dy;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Entity)) {
      return false;
    }
    Entity e = (Entity) obj;
    return id == e.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

}
