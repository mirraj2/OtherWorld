package ow.client.model;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import ow.client.arch.SGraphics;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import ow.client.ImageLoader;
import ow.client.model.effect.Effect;
import ow.client.model.effect.ShipExplosion;

import static com.google.common.base.Preconditions.checkArgument;

public class ClientModel {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ClientModel.class);

  private SortedMap<Integer, Ship> ships = ImmutableSortedMap.of();
  private final List<Planet> planets = Lists.newCopyOnWriteArrayList();
  private final Map<Integer, Shot> shots = Maps.newConcurrentMap();
  private final List<Effect> effects = Lists.newCopyOnWriteArrayList();
  private final List<Runnable> tasksToRun = Lists.newCopyOnWriteArrayList();

  private Ship focus = null;

  public void addShip(Ship ship) {
    if (ships.containsKey(ship.id)) {
      return;
    }
    Map<Integer, Ship> newShipsMap = Maps.newTreeMap(this.ships);
    newShipsMap.put(ship.id, ship);
    this.ships = ImmutableSortedMap.copyOf(newShipsMap);
  }

  public void removeShip(int shipID) {
    checkArgument(ships.containsKey(shipID));

    Map<Integer, Ship> newShipsMap = Maps.newTreeMap(this.ships);
    newShipsMap.remove(shipID);
    this.ships = ImmutableSortedMap.copyOf(newShipsMap);
  }

  public void explodeShip(int shipID) {
    final Ship ship = ships.get(shipID);
    
    removeShip(shipID);
    
    tasksToRun.add(new Runnable() {
      @Override
      public void run() {
        effects.add(new ShipExplosion(ship));
      }
    });
  }

  public void addPlanet(Planet planet) {
    this.planets.add(planet);
  }

  public void addShot(Shot shot) {
    shots.put(shot.id, shot);
  }

  public void removeShot(int shotID) {
    shots.remove(shotID);
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

    List<Integer> expiredShots = Lists.newArrayList();
    for (Shot shot : shots.values()) {
      shot.tick(delta);
      if(shot.hasExpired()){
        expiredShots.add(shot.id);
      }
    }
    for (Integer expiredShot : expiredShots) {
      shots.remove(expiredShot);
    }

    for (Effect effect : effects) {
      effect.tick(delta);
      if (effect.isFinished()) {
        effects.remove(effect);
      }
    }
  }

  public void render(SGraphics g, int w, int h) {
    if (this.focus == null) {
      return;
    }

    for (Runnable r : tasksToRun) {
      r.run();
      tasksToRun.remove(r);
    }

    g.translate(-(focus.x - w / 2), -(focus.y - h / 2));

    for (Planet planet : planets) {
      Image im = ImageLoader.getSlickImage("planets/" + planet.name + ".png");

      g.draw(im, planet.x - im.getWidth() / 2, planet.y - im.getHeight() / 2);
    }

    for (Effect effect : effects) {
      effect.render(g);
    }

    for (Ship ship : ships.values()) {
      render(g, ship);
    }

    for (Shot shot : shots.values()) {
      g.setColor(Color.orange).fillCircle(shot.x, shot.y, 2);
    }
  }

  public Rectangle getCameraBounds(int screenWidth, int screenHeight) {
    return new Rectangle((int) (focus.x - screenWidth / 2), (int) (focus.y - screenHeight / 2),
        screenWidth, screenHeight);
  }

  private void render(SGraphics g, Ship ship) {
    Image image = ship.getImage();

    double x = ship.x - image.getWidth() / 2;
    double y = ship.y - image.getHeight() / 2;

    double r = ship.rotation;

    g.rotate(-r, ship.x, ship.y);
    g.draw(image, x, y);
    g.rotate(r, ship.x, ship.y);

    y -= 6;
    int barHeight = 4;
    double p = ship.hp / ship.maxHP;

    g.setColor(Color.black);
    g.fillRect(x, y, image.getWidth(), barHeight);

    if (p < .2) {
      g.setColor(Color.red);
    } else if (p < .5) {
      g.setColor(Color.yellow);
    } else {
      g.setColor(Color.green);
    }
    g.fillRect(x + 1, y + 1, p * image.getWidth() - 2, barHeight - 2);
  }

}
