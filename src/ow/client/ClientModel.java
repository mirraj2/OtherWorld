package ow.client;

import java.util.List;

import com.google.common.collect.Lists;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

public class ClientModel {

  private List<Ship> ships = Lists.newCopyOnWriteArrayList();

  private Ship focus = null;

  public void add(Ship ship) {
    this.ships.add(ship);
  }

  public void focus(Ship ship) {
    this.focus = ship;
  }

  public void render(SGraphics g, int w, int h) {
    g.setColor(Color.black).fillRect(0, 0, w, h);

    if (this.focus == null) {
      return;
    }

    g.translate(-(focus.getX() - w / 2), -(focus.getY() - h / 2));

    for (Ship ship : ships) {
      render(g, ship);
    }
  }

  private void render(SGraphics g, Ship ship) {
    Image image = ImageLoader.getSlickImage(ship.getImage());
    double x = ship.getX() - image.getWidth() / 2;
    double y = ship.getY() - image.getHeight() / 2;

    double r = ship.getRotation() - Math.PI / 2;

    g.rotate(r, ship.getX(), ship.getY());
    g.draw(image, x, y);
    g.rotate(-r, ship.getX(), ship.getY());
  }

}
