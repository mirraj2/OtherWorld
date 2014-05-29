package ow.server;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.log4j.Logger;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Shape;
import ow.common.Faction;
import ow.common.ShipType;
import ow.server.brain.FedSpawner;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;

public class World {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(World.class);

  private static final double MIN_DIST_BETWEEN_PLANETS = 1000;

  private static final Random rand = new Random();

  private final OWServer server;
  private Map<Integer, Ship> ships = Maps.newConcurrentMap();
  private List<Shot> shots = Lists.newCopyOnWriteArrayList();
  private List<AI> ais = Lists.newCopyOnWriteArrayList();
  private RTree<Planet> planets = new RTree<>();

  private Planet startingPlanet;

  public World(OWServer server) {
    this.server = server;

    generatePlanets();
    
    add(new Ship(Faction.EXPLORERS, ShipType.STATION, startingPlanet.x + 300,
        startingPlanet.y + 200));

    for (Planet planet : planets) {
      if (planet != startingPlanet) {
        Ship fedStation = new Ship(Faction.FEDERATION, ShipType.STATION, planet.x + 300,
            planet.y - 100);
        add(fedStation);
        addAI(new FedSpawner(this, fedStation));
      }
    }

    Executors.newSingleThreadExecutor().execute(updater);
  }

  private void generatePlanets() {
    int mapSize = 20000;

    outer: for (int i = 0; i < 100; i++) {
      int x = rand.nextInt(mapSize);
      int y = rand.nextInt(mapSize);

      if (planets.find(x, y, MIN_DIST_BETWEEN_PLANETS) != null) {
        i--;
        continue outer;
      }

      planets.add(generatePlanetAt(x, y), x, y);
    }

    startingPlanet = getFirst(planets, null);

    for (Planet p : planets) {
      generateConnections(p);
    }

    joinGraph();
  }

  /**
   * Make sure there are no isolated subsectors
   */
  private void joinGraph() {
    List<Set<Planet>> subsectors = getSubsectors();

    while (subsectors.size() > 1) {
      Set<Planet> sectorA = subsectors.get(0);
      Planet p2 = getClosestOutOfSector(sectorA, null, planets);
      
      Set<Planet> sectorB = getSector(p2, subsectors);
      Planet p1 = getClosestOutOfSector(sectorB, p2, sectorA);
      
      p1.connectTo(p2);

      sectorB.addAll(sectorA);
      subsectors.remove(0);
    }
  }
  
  private Set<Planet> getSector(Planet p, List<Set<Planet>> subsectors) {
    for (Set<Planet> s : subsectors) {
      if (s.contains(p)) {
        return s;
      }
    }
    throw new IllegalStateException();
  }

  private Planet getClosestOutOfSector(Set<Planet> sector, Planet p1, Iterable<Planet> searchSpace) {
    if (p1 == null) {
      p1 = getFirst(sector, null);
    }

    Planet closest = null;
    double d = -1;
    for (Planet p2 : searchSpace) {
      if (!sector.contains(p2)) {
        double dd = p1.distSquared(p2);
        if (closest == null || dd < d) {
          closest = p2;
          d = dd;
        }
      }
    }
    return closest;
  }

  private List<Set<Planet>> getSubsectors() {
    Set<Planet> seen = Sets.newHashSet();
    List<Set<Planet>> ret = Lists.newArrayList();

    for (Planet p : planets) {
      if (seen.contains(p)) {
        continue;
      }

      Set<Planet> currentSubsector = Sets.newHashSet();
      walkSubsector(p, currentSubsector);
      ret.add(currentSubsector);
      seen.addAll(currentSubsector);
    }

    return ret;
  }

  private void walkSubsector(Planet p, Set<Planet> subsector) {
    if (!subsector.add(p)) {
      return;
    }
    for (Planet p2 : p.connections) {
      walkSubsector(p2, subsector);
    }
  }

  private void generateConnections(final Planet p) {
    int numConnections = 1;
    while (true) {
      double r = rand.nextDouble();
      if (r < 1 / (numConnections + .5)) {
        numConnections++;
      } else {
        break;
      }
    }

    List<Planet> targets = Ordering.from(new Comparator<Planet>() {
      @Override
      public int compare(Planet a, Planet b) {
        double distA = p.distSquared(a);
        double distB = p.distSquared(b);
        return (int) Math.signum(distA - distB);
      }
    }).leastOf(planets, numConnections + 1);

    for (Planet to : targets) {
      p.connectTo(to);
    }
  }

  private Planet generatePlanetAt(int x, int y) {
    return new Planet(random(Planet.types), randomColor(), x, y);
  }

  private int randomColor() {
    while (true) {
      int r = rand.nextInt(255);
      int g = rand.nextInt(255);
      int b = rand.nextInt(255);

      if (r + g + b >= 40) {
        return new Color(r, g, b).getRGB();
      }
    }
  }

  private <T> T random(List<T> c) {
    return c.get(rand.nextInt(c.size()));
  }

  public void fire(Ship shooter) {
    List<Shot> ret = Lists.newArrayList();
    for (Point p : shooter.gunLocations) {
      double r = -shooter.rotation;
      double xOffset = p.x * Math.cos(r) - p.y * Math.sin(r);
      double yOffset = p.y * Math.cos(r) + p.x * Math.sin(r);
      ret.add(new Shot(shooter, shooter.x + xOffset, shooter.y + yOffset, shooter.rotation));
    }

    shots.addAll(ret);

    server.onShotsFired(ret);
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
      if (ship.distSquared(x2, y2) > 1000 * 1000) {
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
    return new Point((int) startingPlanet.x + 300, (int) startingPlanet.y + 100);
  }

  public Iterable<Planet> getPlanets() {
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
