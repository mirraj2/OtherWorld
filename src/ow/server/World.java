package ow.server;

import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ow.common.ShipType;

import static com.google.common.base.Preconditions.checkNotNull;

public class World {

  private Map<Integer, Ship> ships = Maps.newConcurrentMap();
  private List<Planet> planets = Lists.newArrayList();

  private final Point spawnLocation = new Point(1400, 900);
  
  public World() {
    planets.add(new Planet("Mars", 1000, 1000));

    Ship station = new Ship(ShipType.STATION, new Point(spawnLocation.x, spawnLocation.y + 80));
    add(station);
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
