package ow.client.model;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.jason.SGraphics;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import ow.client.model.effect.Effect;
import ow.client.model.effect.ShipExplosion;

import static com.google.common.base.Preconditions.checkArgument;

public class ClientModel {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ClientModel.class);

  private static final Color connectionColor = new Color(1f, 1f, 1f, .3f);

  private SortedMap<Integer, Ship> ships = ImmutableSortedMap.of();
  private final Map<Integer, Planet> planets = Maps.newConcurrentMap();
  private final Map<Integer, Shot> shots = Maps.newConcurrentMap();
  private final List<Effect> effects = Lists.newCopyOnWriteArrayList();
  private final List<Runnable> tasksToRun = Lists.newCopyOnWriteArrayList();

  private List<Integer> expiredShots = Lists.newArrayList();
  private List<Rectangle> debugRects = Lists.newArrayList();

  private double zoom = 1;

  private Ship focus = null;
  private boolean showDebugRects = false;

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

  public void explodeShip(final Ship ship) {
    removeShip(ship.id);
    
    tasksToRun.add(new Runnable() {
      @Override
      public void run() {
        effects.add(new ShipExplosion(ship));
      }
    });
  }

  public void addPlanet(Planet planet) {
    this.planets.put(planet.id, planet);
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

  public Ship getFocus() {
    return focus;
  }

  public void tick(int delta) {
    for (Ship ship : ships.values()) {
      ship.tick(delta);
    }

    expiredShots.clear();
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

    g.zoom(zoom);
    g.translate(focus.x - (w / zoom) / 2, focus.y - (h / zoom) / 2);
    g.clip(w / zoom, h / zoom);

    drawConnections(g);

    for (Planet planet : planets.values()) {
      Image im = planet.getImage();
      g.draw(im, planet.x - im.getWidth() / 2, planet.y - im.getHeight() / 2);
    }

    for (Effect effect : effects) {
      effect.render(g);
    }

    for (Ship ship : ships.values()) {
      if (ship.active) {
        render(g, ship);
      }
    }

    for (Shot shot : shots.values()) {
      g.color(Color.orange).fillCircle(shot.x, shot.y, 2);
    }

    if (showDebugRects) {
      g.color(Color.orange);
      for (Rectangle r : debugRects) {
        g.draw(r.x, r.y, r.width, r.height);
      }
    }
  }

  private void drawConnections(SGraphics g) {
    g.color(connectionColor);
    Set<Integer> seen = Sets.newHashSet();
    for (Planet planet : planets.values()) {
      for (int id : planet.connections) {
        if (!seen.add(hash(planet.id, id))) {
          continue;
        }
        Planet p2 = planets.get(id);
        drawConnection(g, planet, p2);
      }
    }
  }

  private int hash(int idA, int idB) {
    if (idA > idB) {
      return idB + idA * Short.MAX_VALUE;
    } else {
      return idA + idB * Short.MAX_VALUE;
    }
  }

  private void drawConnection(SGraphics g, Planet a, Planet b) {
    g.line(8.0, a.x, a.y, b.x, b.y);
  }

  public Rectangle getCameraBounds(int screenWidth, int screenHeight) {
    if (focus == null) {
      return new Rectangle(0, 0, screenWidth, screenHeight);
    }

    return new Rectangle((int) (focus.x - screenWidth / 2), (int) (focus.y - screenHeight / 2),
        screenWidth, screenHeight);
  }

  private void render(SGraphics g, Ship ship) {
    Image image = ship.getImage();

    double x = ship.x - image.getWidth() / 2;
    double y = ship.y - image.getHeight() / 2;

    if (g.clipMiss(x, y, image.getWidth(), image.getHeight())) {
      return;
    }

    g.draw(image, x, y);

    y -= 6;
    int barHeight = 4;
    double p = ship.hp / ship.maxHP;

    g.color(Color.black);
    g.fillRect(x, y, image.getWidth(), barHeight);

    if (p < .2) {
      g.color(Color.red);
    } else if (p < .5) {
      g.color(Color.yellow);
    } else {
      g.color(Color.green);
    }
    g.fillRect(x + 1, y + 1, p * image.getWidth() - 2, barHeight - 2);
  }

  public void zoomOut() {
    zoom /= 1.3;
    // zoom /= 2;
  }

  public void zoomIn() {
    zoom *= 1.3;
    // zoom *= 2;
  }

  public void setDebugRects(List<Rectangle> debugRects) {
    this.debugRects = debugRects;
  }

  public void toggleDebugRects() {
    this.showDebugRects = !showDebugRects;
  }

}
