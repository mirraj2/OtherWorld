package ow.client.arch;

import java.awt.Rectangle;

import com.google.common.base.Throwables;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Shape;

public class SGraphics {

  private final Graphics g;

  private Rectangle clip = new Rectangle(0, 0, 0, 0);

  public SGraphics(Graphics g) {
    this.g = g;
    g.setAntiAlias(true);
  }

  public SGraphics setColor(Color c) {
    g.setColor(c);
    return this;
  }

  public SGraphics fill(Shape shape) {
    g.fill(shape);
    return this;
  }

  public SGraphics draw(double x, double y, double w, double h) {
    if (clipMiss(x, y, w, h)) {
      return this;
    }
    g.drawRect((float) x, (float) y, (float) w, (float) h);
    return this;
  }

  public SGraphics fillRect(double x, double y, double w, double h) {
    if (clipMiss(x, y, w, h)) {
      return this;
    }
    g.fillRect((float) x, (float) y, (float) w, (float) h);
    return this;
  }

  public SGraphics fillCircle(double x, double y, double radius) {
    return fillOval((float) (x - radius), (float) (y - radius), (float) (radius * 2),
        (float) (radius * 2));
  }

  private SGraphics fillOval(double x, double y, double w, double h) {
    if (clipMiss(x, y, w, h)) {
      return this;
    }
    g.fillOval((float) x, (float) y, (float) w, (float) h);
    return this;
  }

  public SGraphics translate(double x, double y) {
    g.translate((float) -x, (float) -y);
    this.clip.x += x;
    this.clip.y += y;
    return this;
  }

  public SGraphics rotate(double r, double x, double y) {
    g.rotate((float) x, (float) y, (float) Math.toDegrees(r));
    return this;
  }

  public SGraphics draw(Image image, double x, double y) {
    if (clipMiss(x, y, image.getWidth(), image.getHeight())) {
      return this;
    }

    g.drawImage(image, (float) x, (float) y);
    return this;
  }

  public SGraphics line(double lineWidth, double x, double y, double xx, double yy) {
    if (clipMiss(Math.min(x, xx), Math.min(y, yy), Math.abs(x - xx), Math.abs(y - yy))) {
      return this;
    }

    float oldWidth = g.getLineWidth();
    g.setLineWidth((float) lineWidth);
    g.drawLine((float) x, (float) y, (float) xx, (float) yy);
    g.setLineWidth(oldWidth);
    return this;
  }

  public SGraphics zoom(double zoom) {
    g.scale((float) zoom, (float) zoom);
    return this;
  }

  public SGraphics clip(double w, double h) {
    this.clip.width = (int) w;
    this.clip.height = (int) h;
    return this;
  }

  public SGraphics text(String text, double x, double y) {
    g.drawString(text, (float) x, (float) y);
    return this;
  }

  public SGraphics textCentered(String text, int w, int h) {
    Font font = g.getFont();
    int sw = font.getWidth(text);
    int sh = font.getHeight(text);

    return text(text, w / 2 - sw / 2, h / 2 - sh / 2);
  }

  public SGraphics font(Font font) {
    g.setFont(font);
    return this;
  }

  public SGraphics push() {
    g.pushTransform();
    return this;
  }

  public SGraphics pop() {
    g.popTransform();
    clip.x = 0;
    clip.y = 0;
    clip.width = 0;
    clip.height = 0;
    return this;
  }

  public boolean clipMiss(double x, double y, double w, double h) {
    if (clip.width == 0) {
      return false;
    }
    if (x > clip.getMaxX() || y > clip.getMaxY()) {
      return true;
    }
    if (x + w < clip.x || y + h < clip.y) {
      return true;
    }
    return false;
  }

  public SGraphics destroy() {
    g.destroy();
    return this;
  }

  public static SGraphics create(Image im) {
    try {
      return new SGraphics(im.getGraphics());
    } catch (SlickException e) {
      throw Throwables.propagate(e);
    }
  }

}
