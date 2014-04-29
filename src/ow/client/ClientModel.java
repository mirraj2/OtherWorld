package ow.client;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.newdawn.slick.Image;
import ow.common.Planet;

public class ClientModel {

  private Map<Integer, Ship> ships = Maps.newConcurrentMap();
  private List<Planet> planets = Lists.newCopyOnWriteArrayList();

  private Ship focus = null;

  public void add(Ship ship) {
    if (ships.containsKey(ship.id)) {
      return;
    }
    this.ships.put(ship.id, ship);
  }

  public void add(Planet planet) {
    this.planets.add(planet);
  }

  public Ship getShip(int id) {
    return ships.get(id);
  }

  public void focus(Ship ship) {
    this.focus = ship;
  }

  public void tick(int delta) {
    for (Ship ship : ships.values()) {
      ship.tick(delta);
    }
  }

  public void render(SGraphics g, int w, int h) {
    if (this.focus == null) {
      return;
    }

    g.translate(-(focus.x - w / 2), -(focus.y - h / 2));

    for (Planet planet : planets) {
      Image im = ImageLoader.getSlickImage("planets/" + planet.name + ".png");

      g.draw(im, planet.x - im.getWidth() / 2, planet.y - im.getHeight() / 2);
    }

    for (Ship ship : ships.values()) {
      render(g, ship);
    }
  }

  public Rectangle getCameraBounds(int screenWidth, int screenHeight) {
    return new Rectangle((int) (focus.x - screenWidth / 2), (int) (focus.y - screenHeight / 2),
        screenWidth, screenHeight);
  }

  private void render(SGraphics g, Ship ship) {
    Image image = ImageLoader.getSlickImage("ships/" + ship.type.getImageName());

    double x = ship.x - image.getWidth() / 2;
    double y = ship.y - image.getHeight() / 2;

    double r = ship.rotation;

    g.rotate(-r, ship.x, ship.y);
    g.draw(image, x, y);
    g.rotate(r, ship.x, ship.y);
  }

}
