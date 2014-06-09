package ow.server.arch.qtree;

public class Rect {
  final double x, y, w, h;

  public Rect(double x, double y, double w, double h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Rect)) {
      return false;
    }
    Rect r = (Rect) obj;
    return x == r.x && y == r.y && w == r.w && h == r.h;
  }

  public boolean intersects(Rect other) {
    if ((x > other.x + other.w) || (x + w < other.x)) {
      return false;
    }
    if ((y > other.y + other.h) || (y + h < other.y)) {
      return false;
    }
    return true;
  }

  public boolean contains(double x, double y) {
    if (x < this.x || y < this.y) {
      return false;
    }

    if (x >= this.x + w || y >= this.y + h) {
      return false;
    }

    return true;
  }

  public boolean contains(Rect other) {
    return other.x >= x && other.y >= y && other.maxX() <= other.maxX() && other.maxY() <= maxY();
  }

  public Rect centerOn(double xx, double yy) {
    return new Rect(xx - w / 2, yy - h / 2, w, h);
  }

  public Rect doubleSize() {
    return new Rect(x - w / 2, y - h / 2, w * 2, h * 2);
  }

  public double centerX() {
    return x + w / 2;
  }

  public double centerY() {
    return y + h / 2;
  }

  public double maxX() {
    return x + w;
  }

  public double maxY() {
    return y + h;
  }

  @Override
  public String toString() {
    return x + ", " + y + ", " + w + ", " + h;
  }
}
