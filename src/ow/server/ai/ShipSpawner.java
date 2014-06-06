package ow.server.ai;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ow.common.ShipType;
import ow.server.model.Ship;
import ow.server.model.World;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ShipSpawner extends ShipAI {

  private final List<Ship> shipsSpawned = Lists.newArrayList();
  private final Task spawnTask = Task.every(1000, TimeUnit.MILLISECONDS);
  private final ShipType spawnType;
  private final int maxShips;
  private final double spawnChance;

  public ShipSpawner(World world, Ship spawner, ShipType spawnType, int maxShips,
      double spawnChance) {
    super(world, spawner);

    checkArgument(maxShips > 0);
    checkArgument(spawnChance > 0);

    this.spawnType = checkNotNull(spawnType);
    this.maxShips = maxShips;
    this.spawnChance = spawnChance;
  }

  @Override
  public boolean run(double millis) {
    // remove any dead ships
    for (int i = shipsSpawned.size() - 1; i >= 0; i--) {
      if (shipsSpawned.get(i).isDead()) {
        shipsSpawned.remove(i);
      }
    }

    if (shipsSpawned.size() < maxShips && spawnTask.isReady()) {
      if (Math.random() < spawnChance) {
        spawn();
      }
    }

    return false;
  }

  private void spawn() {
    double r = Math.random() * Math.PI * 2;
    Ship s = new Ship(ship.faction, spawnType, ship.x, ship.y).rotation(r);
    shipsSpawned.add(s);
    world.add(s);
    world.addAI(new ProtectAI(world, s, ship));
  }

  public boolean isAtMaxCapacity() {
    return shipsSpawned.size() == maxShips;
  }

}
