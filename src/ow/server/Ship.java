package ow.server;

import java.awt.Point;

public class Ship {

  private double x, y;

  public Ship(Point initialLocation) {
    setLocation(initialLocation);
  }

  public void setLocation(Point p) {
    this.x = p.x;
    this.y = p.y;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

}
