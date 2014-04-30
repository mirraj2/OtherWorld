package ow.common;

public class OMath {

  public static double distanceSquared(double x, double x2, double y, double y2) {
    double xDiff = x - x2;
    double yDiff = y - y2;
    return xDiff * xDiff + yDiff * yDiff;
  }

  public static double getTargetRotation(double x, double y, double targetX, double targetY) {
    double ret = Math.atan2(targetX - x, targetY - y) - Math.PI / 2;
    if (ret < 0) {
      ret += Math.PI * 2;
    }
    return ret;
  }

}