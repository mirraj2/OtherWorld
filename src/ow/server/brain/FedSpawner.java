package ow.server.brain;

import ow.common.Faction;
import ow.common.ShipType;
import ow.server.AI;
import ow.server.Ship;
import ow.server.World;

public class FedSpawner extends AI {

  private double timeSinceLastSpawnAttempt = 0;

  private final Ship station;

  public FedSpawner(World world, Ship station) {
    super(world);
    this.station = station;
  }

  @Override
  public void tick(double millis) {
    timeSinceLastSpawnAttempt += millis;
    if (timeSinceLastSpawnAttempt >= 500) {
      spawnNewShips();
      timeSinceLastSpawnAttempt = 0;
    }
  }

  private void spawnNewShips() {
    int numEnemyShips = 0;
    for (Ship ship : world.getShips()) {
      if (ship.faction == Faction.FEDERATION) {
        numEnemyShips++;
      }
    }

    if (numEnemyShips > 10) {
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
    Ship ship = new Ship(Faction.FEDERATION, ShipType.MINI, x, y).rotation(r);
    world.add(ship);
    world.addAI(new ProtectAI(world, ship, station));
  }

}
