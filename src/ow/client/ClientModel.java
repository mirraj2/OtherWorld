package ow.client;

import java.awt.Rectangle;
import java.util.List;

import org.newdawn.slick.Image;

import com.google.common.collect.Lists;

public class ClientModel {

  private List<Ship> ships = Lists.newCopyOnWriteArrayList();

  private Ship focus = null;

  public void add(Ship ship) {
    this.ships.add(ship);
  }

  public void focus(Ship ship) {
    this.focus = ship;
  }

  public void tick(int delta) {
    for (Ship ship : ships) {
      ship.tick(delta);
    }
  }

  public void render(SGraphics g, int w, int h) {
    if (this.focus == null) {
      return;
    }

    g.translate(-(focus.x - w / 2), -(focus.y - h / 2));

    for (Ship ship : ships) {
      render(g, ship);
    }
  }

  public Rectangle getCameraBounds(int screenWidth, int screenHeight) {
    return new Rectangle((int) (focus.x - screenWidth / 2), (int) (focus.y - screenHeight / 2),
        screenWidth, screenHeight);
  }

  private void render(SGraphics g, Ship ship) {
    Image image = ImageLoader.getSlickImage(ship.image);
    double x = ship.x - image.getWidth() / 2;
    double y = ship.y - image.getHeight() / 2;

    double r = ship.rotation;

    g.rotate(-r, ship.x, ship.y);
    g.draw(image, x, y);
    g.rotate(r, ship.x, ship.y);
  }

}
