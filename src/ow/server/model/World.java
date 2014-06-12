package ow.server.model;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.log4j.Logger;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Shape;
import ow.common.Faction;
import ow.server.OWServer;
import ow.server.ai.AI;
import ow.server.ai.ShipAI;
import ow.server.arch.Task;
import ow.server.arch.qtree.QuadTree;
import ow.server.arch.qtree.Query;
import ow.server.gen.TwoFactionScenario;
import ow.server.sync.GameSync;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;

public class World {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(World.class);

  private final OWServer server;

  private Map<Integer, Ship> ships = Maps.newConcurrentMap();
  private QuadTree<Ship> shipTree;

  private List<Shot> shots = Lists.newCopyOnWriteArrayList();
  private List<AI> ais = Lists.newCopyOnWriteArrayList();
  private Map<Integer, ShipAI> shipAIs = Maps.newConcurrentMap();
  private final QuadTree<Planet> planets;

  private final Planet startingPlanet;
  private final GameSync sync;

  private Task refreshTreeTask = Task.every(1, TimeUnit.SECONDS);

  public World(OWServer server) {
    this.server = server;
    this.sync = server.getSync();

    this.planets = new GraphGenerator().generatePlanets();
    startingPlanet = getFirst(planets, null);
    
    this.shipTree = new QuadTree<>(planets.getBounds());

    // new AdventureModeGenerator(this).generate();
    new TwoFactionScenario(this).generate();

    Executors.newSingleThreadExecutor().execute(updater);
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

    sync.onShotsFired(ret);
  }

  /**
   * Update all connected players about changes to this ship.
   */
  public void sendUpdate(Ship ship) {
    sync.markUpdated(ship);
  }

  private void tick(double millis) {
    for (Ship ship : ships.values()) {
      ship.tick(millis);
    }

    tickProjectiles(millis);

    if (refreshTreeTask.isReady()) {
      refreshTree();
    }

    for (AI brain : ais) {
      if (brain.tick(millis)) {
        ais.remove(brain);
      }
    }
  }

  private void refreshTree() {
    Stopwatch watch = Stopwatch.createStarted();
    shipTree = new QuadTree<>(shipTree.getBounds());
    for (Ship ship : ships.values()) {
      shipTree.add(ship, ship.x, ship.y);
    }
    if (OWServer.ENABLE_QUADTREE_DEBUG) {
      server.sendToAll(shipTree.toJson());
    }
    System.out.println("Refreshed Quadtree (" + ships.size() + " ships)" + " (" + watch + ")");
  }

  private void tickProjectiles(double millis) {
    Set<Shot> expiredShots = Sets.newHashSet();
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
    List<Ship> nearby = shipTree.select(Query.start(x1, y1).radius(100).end());
    for (Ship ship : nearby) {
      if (ship.faction == shooterFaction) {
        // no friendly fire
        continue;
      }
      Shape collisionArea = ship.getCollisionArea();
      if (collisionArea.contains(line) || line.intersects(collisionArea)) {
        return ship;
      }
    }
    return null;
  }

  public Ship addShip(Ship ship) {
    checkNotNull(ship);

    this.ships.put(ship.id, ship);
    this.shipTree.add(ship, ship.x, ship.y);

    return ship;
  }

  public void removeShip(Ship ship) {
    checkNotNull(ship);

    this.ships.remove(ship.id);
    this.shipTree.remove(ship);
  }

  public AI addAI(AI ai) {
    this.ais.add(ai);
    if (ai instanceof ShipAI) {
      Ship ship = ((ShipAI) ai).getShip();
      this.shipAIs.put(ship.id, (ShipAI) ai);
    }
    return ai;
  }

  public ShipAI getAI(Ship ship) {
    return this.shipAIs.get(ship.id);
  }

  public Ship getShip(int id) {
    return ships.get(id);
  }

  public Point getPlayerSpawnLocation() {
    return new Point((int) startingPlanet.x + 300, (int) startingPlanet.y + 100);
  }

  public QuadTree<Planet> getPlanets() {
    return planets;
  }

  public QuadTree<Ship> getShips() {
    return shipTree;
  }

  public Planet getStartingPlanet() {
    return startingPlanet;
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
          if (millis > 10) {
            System.out.println("tick: " + millis);
          }
          tick(millis);
        } catch (Exception e) {
          e.printStackTrace();
        }

        lastTime = now;
        if (millis < 2) {
          Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
        }
      }
    }
  };

}
