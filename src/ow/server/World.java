package ow.server;

import java.awt.Point;
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
import org.newdawn.slick.geom.Rectangle;
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
      double xOffset = p.x * Math.cos(shooter.rotation) - p.y * Math.sin(shooter.rotation);
      double yOffset = -p.y * Math.cos(shooter.rotation) - p.x * Math.sin(shooter.rotation);
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

    List<Shot> expiredShots = Lists.newArrayList();
    for (Shot shot : shots) {
      double x1 = shot.x, y1 = shot.y;
      shot.tick(millis);
      double x2 = shot.x, y2 = shot.y;

      Ship intersection = getIntersection(x1, y1, x2, y2, shot.shooter.faction);
      if (intersection != null) {
        // explode the shot
      }

      if (shot.hasExpired()) {
        expiredShots.add(shot);
      }
    }
    shots.removeAll(expiredShots);

    for (AI brain : ais) {
      brain.tick(millis);
    }
  }

  private Ship getIntersection(double x1, double y1, double x2, double y2, Faction shooterFaction) {
    Line line = new Line((float) x1, (float) y1, (float) x2, (float) y2);
    for (Ship ship : ships.values()) {
      if (ship.faction == shooterFaction) {
        continue;
      }
      // TODO
      if (line.intersects(new Rectangle())) {
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

        tick(millis);

        lastTime = now;
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
      }
    }
  };

}
