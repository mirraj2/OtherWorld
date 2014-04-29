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
import ow.common.ShipType;

import static com.google.common.base.Preconditions.checkNotNull;

public class World {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(World.class);

  private final Point spawnLocation = new Point(1400, 900);

  private Map<Integer, Ship> ships = Maps.newConcurrentMap();
  private List<Planet> planets = Lists.newArrayList();
  private List<Shot> shots = Lists.newCopyOnWriteArrayList();

  public World() {
    planets.add(new Planet("Mars", 1000, 1000));

    Ship station = new Ship(ShipType.STATION, new Point(spawnLocation.x, spawnLocation.y + 80));
    add(station);

    Executors.newSingleThreadExecutor().execute(updater);
  }
  
  public Collection<Shot> fire(Ship shooter) {
    List<Shot> ret = Lists.newArrayList();
    for (Point p : shooter.gunLocations) {
      double xOffset = p.x * Math.cos(shooter.rotation) - p.y * Math.sin(shooter.rotation);
      double yOffset = -p.y * Math.cos(shooter.rotation) - p.x * Math.sin(shooter.rotation);
      ret.add(new Shot(shooter.x + xOffset, shooter.y + yOffset, shooter.rotation));
    }

    shots.addAll(ret);

    return ret;
  }

  private final Runnable updater = new Runnable() {
    @Override
    public void run() {
      long lastTime = System.nanoTime();
      while (true) {
        long now = System.nanoTime();

        double millis = (now - lastTime) / 1000000.0;

        update(millis);

        lastTime = now;
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
      }
    }
  };

  private void update(double millis) {
    for (Ship ship : ships.values()) {
      ship.tick(millis);
    }

    List<Shot> expiredShots = Lists.newArrayList();
    for (Shot shot : shots) {
      shot.tick(millis);
      if (shot.hasExpired()) {
        expiredShots.add(shot);
      }
    }
    shots.removeAll(expiredShots);
  }

  public void add(Ship ship) {
    checkNotNull(ship);
    
    this.ships.put(ship.id, ship);
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

  public Point getSpawnLocation() {
    return spawnLocation;
  }

  public List<Planet> getPlanets() {
    return planets;
  }

}
