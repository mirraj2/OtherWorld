package ow.server.ai;

import java.util.concurrent.TimeUnit;

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

    if (spawner.isAtMaxCapacity()) {
      if (spawnColonyShip.isReady() && Math.random() < .1) {
        spawnColonyShip();
      }
    }

    return false;
  }

  private void spawnColonyShip() {
    // spawn the colony ship
    // give it an AI which makes it search for an uninhabited planet

    // somehow get access to the protectAI of all the ships in this spawner
    // and tell them to protect the colony ship we just spawned

    // make sure the spawner's ship buffer is cleared so that it can keep spawning
  }

}
