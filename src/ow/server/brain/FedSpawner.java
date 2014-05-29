package ow.server.brain;

import java.util.List;

import com.google.common.collect.Lists;
import ow.common.Faction;
import ow.common.ShipType;
import ow.server.AI;
import ow.server.Ship;
import ow.server.World;

public class FedSpawner extends AI {

  private double timeSinceLastSpawnAttempt = 0;

  private final Ship station;
  private final List<Ship> shipsSpawned = Lists.newArrayList();

  public FedSpawner(World world, Ship station) {
    super(world);
    this.station = station;
  }

  @Override
  public boolean tick(double millis) {
    if (station.isDead()) {
      return true;
    }

    timeSinceLastSpawnAttempt += millis;
    if (timeSinceLastSpawnAttempt >= 500) {
      spawnNewShips();
      timeSinceLastSpawnAttempt = 0;
    }
    return false;
  }

  private void spawnNewShips() {
    // TODO clear out the shipsSpawned list

    if (shipsSpawned.size() > 10) {
      return;
    }

    if (Math.random() < .2) {
      spawnFederateShip();
    }
  }

  private void spawnFederateShip() {
    double r = Math.random() * Math.PI * 2;
    double x = station.x;
    double y = station.y;
    Ship ship = new Ship(Faction.FEDERATION, ShipType.SPECTRE, x, y).rotation(r);
    shipsSpawned.add(ship);
    world.add(ship);
    world.addAI(new ProtectAI(world, ship, station));
  }

}
