package ow.server.ai;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ow.common.ShipType;
import ow.server.arch.Task;
import ow.server.model.Ship;
import ow.server.model.World;

/**
 * Once the spawner has spawned enough ships, this AI will try to spawn a colonizing ship.
 * 
 * Once it has a colonizing ship, it will send the colony ship out (with protection) in order
 * to make a new base.
 */
public class ExpandAI extends ShipAI {

  private final ShipSpawner spawner;
  private final Task spawnColonyShip = Task.every(1, TimeUnit.SECONDS);

  public ExpandAI(World world, ShipSpawner spawner) {
    super(world, spawner.getShip());

    this.spawner = spawner;
  }

  @Override
  protected boolean run(double millis) {
    if (spawner.run(millis)) {
      return true;
    }

    if (spawner.getNumShipsSpawned() >= 10) {
      if (spawnColonyShip.isReady() && Math.random() < .1) {
        spawnColonyShip();
      }
    }

    return false;
  }

  private void spawnColonyShip() {
    Ship colonyShip = world.addShip(new Ship(getShip().faction, ShipType.COLONY_SHIP, ship.x, ship.y));
    world.addAI(new ColonizeAI(world, colonyShip));

    List<Ship> protectors = spawner.emptySpawnList();
    for (Ship ship : protectors) {
      ProtectAI ai = (ProtectAI) world.getAI(ship);
      ai.protect(colonyShip).patrolDistance(100);
    }
  }

}
