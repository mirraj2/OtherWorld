package ow.server.ai;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import ow.common.OMath;
import ow.common.ShipType;
import ow.server.arch.qtree.Query;
import ow.server.model.Planet;
import ow.server.model.Ship;
import ow.server.model.World;
import ow.server.model.WorldGenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkArgument;

public class ColonizeAI extends ShipAI {

  private Set<Planet> visited = Sets.newHashSet();

  private Planet destination;

  public ColonizeAI(World world, Ship ship) {
    super(world, ship);

    checkArgument(ship.type == ShipType.COLONY_SHIP);

    setCourseForNextPlanet();
    ship.moving(true);
    world.sendUpdate(ship);
  }

  @Override
  protected boolean run(double millis) {
    double oldR = ship.rotation;
    ship.rotateTo(destination, millis);
    if (!OMath.equals(oldR, ship.rotation)) {
      world.sendUpdate(ship);
    }

    if (ship.inRange(destination, 100)) {
      if (isUninhabited(destination)) {
        // transform into a station
        world.removeShip(ship);
        Ship station = world.addShip(new Ship(ship.faction, ShipType.STATION, ship.x, ship.y));
        ShipSpawner spawner = WorldGenerator.getStationSpawner(world, station);
        world.addAI(spawner);
        world.addAI(new ExpandAI(world, spawner));
        return true;
      } else {
        setCourseForNextPlanet();
      }
    }

    return false;
  }

  private boolean isUninhabited(Planet p) {
    Collection<Ship> ships = world.getShips().select(Query.start(p.x, p.y).radius(300));
    for (Ship ship : ships) {
      if (ship.faction == this.ship.faction && ship.type == ShipType.STATION) {
        return false;
      }
    }
    return true;
  }

  private void setCourseForNextPlanet() {
    Planet nearest = world.getPlanets().singleSelect(Query.start(ship.x, ship.y));
    visited.add(nearest);

    List<Planet> candidates = Lists.newArrayList();

    for (Planet p : nearest.connections) {
      if (!visited.contains(p)) {
        candidates.add(p);
      }
    }

    // travel to a random planet
    if (candidates.isEmpty()) {
      destination = OMath.random(world.getPlanets());
    } else {
      destination = OMath.random(candidates);
    }
  }

}
