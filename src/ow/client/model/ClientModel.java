package ow.client.model;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import ow.client.ImageLoader;
import ow.client.SGraphics;
import ow.common.Faction;

public class ClientModel {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ClientModel.class);

  private Map<Integer, Ship> ships = ImmutableMap.of();
  private List<Planet> planets = Lists.newCopyOnWriteArrayList();
  private List<Shot> shots = Lists.newCopyOnWriteArrayList();

  private Ship focus = null;

  public void add(Ship ship) {
    if (ships.containsKey(ship.id)) {
      return;
    }
    Map<Integer, Ship> newShipsMap = Maps.newTreeMap();
    newShipsMap.putAll(ships);
    newShipsMap.put(ship.id, ship);
    this.ships = ImmutableMap.copyOf(newShipsMap);
  }

  public void add(Planet planet) {
    this.planets.add(planet);
  }

  public void add(Shot shot) {
    shots.add(shot);
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

    List<Shot> expiredShots = Lists.newArrayList();
    for (Shot shot : shots) {
      shot.tick(delta);
      if(shot.hasExpired()){
        expiredShots.add(shot);
      }
    }
    shots.removeAll(expiredShots);
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

    for (Shot shot : shots) {
      g.setColor(Color.orange).fillCircle(shot.x, shot.y, 2);
    }
  }

  public Rectangle getCameraBounds(int screenWidth, int screenHeight) {
    return new Rectangle((int) (focus.x - screenWidth / 2), (int) (focus.y - screenHeight / 2),
        screenWidth, screenHeight);
  }

  private void render(SGraphics g, Ship ship) {
    Image image = ImageLoader.getSlickImage("ships/" + ship.type.getImageName());
    adjustImage(image, ship.faction);

    double x = ship.x - image.getWidth() / 2;
    double y = ship.y - image.getHeight() / 2;

    double r = ship.rotation;

    g.rotate(-r, ship.x, ship.y);
    g.draw(image, x, y);
    g.rotate(r, ship.x, ship.y);
  }

  private void adjustImage(Image image, Faction faction) {
    Color c = faction.getColor();
    image.setImageColor(c.r, c.g, c.b);
  }

}
