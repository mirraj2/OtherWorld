package ow.server.ai;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import ow.common.Faction;
import ow.common.ShipType;
import ow.server.Ship;
import ow.server.World;

public class FedSpawner extends ShipAI {

  private final Task spawnTask = Task.every(500, TimeUnit.MILLISECONDS);
  private final List<Ship> shipsSpawned = Lists.newArrayList();

  public FedSpawner(World world, Ship station) {
    super(world, station);
  }

  @Override
  public boolean run(double millis) {
    // TODO clear out the shipsSpawned list

    if (shipsSpawned.size() < 10 && spawnTask.isReady()) {
      if (Math.random() < .2) {
        spawnFederateShip();
      }
    }

    return false;
  }

  private void spawnFederateShip() {
    double r = Math.random() * Math.PI * 2;
    Ship s = new Ship(Faction.FEDERATION, ShipType.SPECTRE, ship.x, ship.y).rotation(r);
    shipsSpawned.add(s);
    world.add(s);
    world.addAI(new ProtectAI(world, s, ship));
  }

}
