package ow.client.model.effect;

import java.util.List;

import com.google.common.collect.Lists;
import org.newdawn.slick.Image;
import ow.client.arch.SGraphics;
import ow.client.arch.SlickUtils;
import ow.client.model.Ship;

public class ShipExplosion extends Effect {

  private static final float FADE_LENGTH = 15000f; // 15 seconds

  private List<Debris> debris = Lists.newArrayList();
  private int t;

  public ShipExplosion(Ship ship) {
    initialize(ship);
  }

  private void initialize(Ship ship) {
    Image im = SlickUtils.deepCopy(ship.getImage());

    double startX = ship.x - im.getWidth() / 2;
    double startY = ship.y - im.getHeight() / 2;

    int pieces = 3;

    int xGap = im.getWidth() / pieces;
    int yGap = im.getHeight() / pieces;
    for (int i = 0; i < pieces; i++) {
      for (int j = 0; j < pieces; j++) {
        Image sub = im.getSubImage(i * xGap, j * yGap, xGap, yGap);
        debris.add(new Debris(sub, startX + i * xGap, startY + j * yGap, ship));
      }
    }
  }

  @Override
  public void tick(int delta) {
    t += delta;
    for (Debris d : debris) {
      d.tick(delta);
    }
  }

  @Override
  public void render(SGraphics g) {
    for (Debris d : debris) {
      Image im = d.im;
      im.setRotation((float) Math.toDegrees(d.r));
      im.setAlpha(1 - t / FADE_LENGTH);
      g.draw(im, d.x, d.y);
    }
  }

  @Override
  public boolean isFinished() {
    return t > FADE_LENGTH;
  }

  private static class Debris {
    private final Image im;

    private double x, y;
    private double r;

    private final double vx, vy;
    private final double spin = Math.random() - .5;

    public Debris(Image im, double x, double y, Ship ship) {
      this.im = im;
      this.x = x;
      this.y = y;
      this.vx = (Math.cos(ship.rotation) * ship.maxSpeed / 1000) + (Math.random() - .5) / 10;
      this.vy = -(Math.sin(ship.rotation) * ship.maxSpeed / 1000) + (Math.random() - .5) / 10;
    }

    public void tick(int delta) {
      r += spin * delta / 30;
      x += vx * delta;
      y += vy * delta;
    }
  }

}
