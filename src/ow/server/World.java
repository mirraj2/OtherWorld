package ow.server;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.log4j.Logger;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Shape;
import ow.common.Faction;
import ow.common.ShipType;
import ow.server.brain.FedSpawner;

import static com.google.common.base.Preconditions.checkNotNull;

public class World {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(World.class);

  private final OWServer server;
  private Map<Integer, Ship> ships = Maps.newConcurrentMap();
  private List<Planet> planets = Lists.newArrayList();
  private List<Shot> shots = Lists.newCopyOnWriteArrayList();
  private List<AI> ais = Lists.newCopyOnWriteArrayList();

  public World(OWServer server) {
    this.server = server;

    planets.add(new Planet("Mars", 1000, 1000));

    Ship fedStation = new Ship(Faction.FEDERATION, ShipType.STATION, new Point(600, 1000));

    add(new Ship(Faction.EXPLORERS, ShipType.STATION, new Point(1400, 1000)).rotation(Math.PI / 6));
    add(fedStation);
    
    ais.add(new FedSpawner(this, fedStation));

    Executors.newSingleThreadExecutor().execute(updater);
  }
  
  public Collection<Shot> fire(Ship shooter) {
    List<Shot> ret = Lists.newArrayList();
    for (Point p : shooter.gunLocations) {
      double r = -shooter.rotation;
      double xOffset = p.x * Math.cos(r) - p.y * Math.sin(r);
      double yOffset = p.y * Math.cos(r) + p.x * Math.sin(r);
      ret.add(new Shot(shooter, shooter.x + xOffset, shooter.y + yOffset, shooter.rotation));
    }

    shots.addAll(ret);

    return ret;
  }

  /**
   * Update all connected players about changes to this ship.
   */
  public void sendUpdate(Ship ship) {
    server.sendUpdate(ship);
  }

  private void tick(double millis) {
    for (Ship ship : ships.values()) {
      ship.tick(millis);
    }

    tickProjectiles(millis);

    for (AI brain : ais) {
      if (brain.tick(millis)) {
        ais.remove(brain);
      }
    }
  }

  private void tickProjectiles(double millis) {
    List<Shot> expiredShots = Lists.newArrayList();
    for (Shot shot : shots) {
      double x1 = shot.x, y1 = shot.y;
      shot.tick(millis);
      double x2 = shot.x, y2 = shot.y;

      Ship hit = getIntersection(x1, y1, x2, y2, shot.shooter.faction);
      if (hit != null) {
        if (isPixelIntersection(shot, hit)) {
          // explode the shot
          double damage = 8 + Math.random() * 4;
          hit.hp = Math.max(0, hit.hp - damage);

          if (hit.hp == 0) {
            ships.remove(hit.id);
          }

          server.onHit(shot, hit, damage);
          expiredShots.add(shot);
        }
      } else {
        if (shot.hasExpired()) {
          expiredShots.add(shot);
        }
      }
    }
    shots.removeAll(expiredShots);
  }

  // Instead of rotating the image based on the ship's rotation,
  // we can inverse-rotate the shot and then use the regular image.
  private boolean isPixelIntersection(Shot shot, Ship hit) {
    BufferedImage bi = hit.image;

    double x = shot.x - hit.x;
    double y = shot.y - hit.y;
    double r = hit.rotation;

    double xOffset = x * Math.cos(r) - y * Math.sin(r);
    double yOffset = y * Math.cos(r) + x * Math.sin(r);

    double x2 = xOffset;
    double y2 = yOffset;

    int xPixel = (int) (bi.getWidth() / 2 + x2);
    int yPixel = (int) (bi.getHeight() / 2 + y2);

    if (xPixel < 0 || yPixel < 0 || xPixel >= bi.getWidth() || yPixel >= bi.getHeight()) {
      return false;
    }

    int rgb = bi.getRGB(xPixel, yPixel);

    return rgb != 0;
  }

  private Ship getIntersection(double x1, double y1, double x2, double y2, Faction shooterFaction) {
    Line line = new Line((float) x1, (float) y1, (float) x2, (float) y2);
    for (Ship ship : ships.values()) {
      if (ship.faction == shooterFaction) {
        continue;
      }
      Shape collisionArea = ship.getCollisionArea();
      if (collisionArea.contains(line) || line.intersects(collisionArea)) {
        return ship;
      }
    }
    return null;
  }

  public void add(Ship ship) {
    checkNotNull(ship);
    
    this.ships.put(ship.id, ship);
    server.onShipAdded(ship);
  }

  public void addAI(AI ai) {
    this.ais.add(ai);
  }

  public Ship getShip(int id) {
    return ships.get(id);
  }

  public void remove(Ship ship) {
    checkNotNull(ship);

    this.ships.remove(ship.id);
  }

  public Collection<Ship> getShips() {
    return ImmutableList.copyOf(ships.values());
  }

  public Point getPlayerSpawnLocation() {
    return new Point(1400, 900);
  }

  public List<Planet> getPlanets() {
    return planets;
  }

  public OWServer getServer() {
    return server;
  }

  private final Runnable updater = new Runnable() {
    @Override
    public void run() {
      long lastTime = System.nanoTime();
      while (true) {
        long now = System.nanoTime();

        double millis = (now - lastTime) / 1000000.0;

        try {
          tick(millis);
        } catch (Exception e) {
          e.printStackTrace();
        }

        lastTime = now;
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
      }
    }
  };

}
