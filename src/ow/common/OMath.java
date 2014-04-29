package ow.common;

public class OMath {

  public static double distanceSquared(double x, double x2, double y, double y2) {
    double xDiff = x - x2;
    double yDiff = y - y2;
    return xDiff * xDiff + yDiff * yDiff;
  }

}
