package ow.client;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class SGraphics {

  private final Graphics g;

  public SGraphics(Graphics g) {
    this.g = g;
  }

  public SGraphics setColor(Color c) {
    g.setColor(c);
    return this;
  }

  public SGraphics fillRect(double x, double y, double w, double h) {
    g.fillRect((float) x, (float) y, (float) w, (float) h);
    return this;
  }

  public SGraphics translate(double x, double y) {
    g.translate((float) x, (float) y);
    return this;
  }

  public SGraphics rotate(double r, double x, double y) {
    g.rotate((float) x, (float) y, (float) Math.toDegrees(r));
    return this;
  }

  public SGraphics draw(Image image, double x, double y) {
    g.drawImage(image, (float) x, (float) y);
    return this;
  }

  public SGraphics push() {
    g.pushTransform();
    return this;
  }

  public SGraphics pop() {
    g.popTransform();
    return this;
  }

}
