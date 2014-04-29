package ow.server;

import java.awt.Point;

public class Entity {

  private static int ID_COUNTER = 0;

  public final int id = ID_COUNTER++;
  public double x, y;

  public void setLocation(Point p) {
    this.x = p.x;
    this.y = p.y;
  }

}
