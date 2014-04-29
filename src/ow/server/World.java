package ow.server;

import java.awt.Point;
import java.util.List;

import com.google.common.collect.Lists;
import ow.common.Planet;
import ow.common.ShipType;

import static com.google.common.base.Preconditions.checkNotNull;

public class World {

  private List<Ship> ships = Lists.newArrayList();
  private List<Planet> planets = Lists.newArrayList();

  private final Point spawnLocation = new Point(1400, 900);
  
  public World() {
    planets.add(new Planet("Mars", 1000, 1000));

    Ship station = new Ship(ShipType.STATION, new Point(spawnLocation.x, spawnLocation.y + 80));
    add(station);
  }

  public void add(Ship ship) {
    checkNotNull(ship);

    this.ships.add(ship);
  }

  public void remove(Ship ship) {
    checkNotNull(ship);

    this.ships.remove(ship);
  }

  public List<Ship> getShips() {
    return ships;
  }

  public Point getSpawnLocation() {
    return spawnLocation;
  }

  public List<Planet> getPlanets() {
    return planets;
  }

}
