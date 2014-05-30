package ow.server.model;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ow.server.arch.RTree;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import static com.google.common.collect.Iterables.getFirst;

public class GraphGenerator {

  private static final double MIN_DIST_BETWEEN_PLANETS = 1600;

  private static final Random rand = new Random();

  private RTree<Planet> planets = new RTree<>();

  public RTree<Planet> generatePlanets() {
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

    for (Planet p : planets) {
      generateConnections(p);
    }

    joinGraph();

    return planets;
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

}
