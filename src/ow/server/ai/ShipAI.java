package ow.server.ai;

import com.google.common.base.Predicate;
import ow.server.Ship;
import ow.server.World;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;

public abstract class ShipAI extends AI {

  protected final Ship ship;

  public ShipAI(World world, Ship ship) {
    super(world);

    this.ship = ship;
  }

  @Override
  public boolean tick(double millis) {
    if(ship.isDead()){
      return true;
    }
    return run(millis);
  }

  protected Iterable<Ship> getNearbyEnemies(int radius) {
    return filter(world.getNearbyShips(ship, radius), enemy);
  }

  protected Ship getClosestEnemy(int radius) {
    return getFirst(getNearbyEnemies(radius), null);
  }

  protected abstract boolean run(double millis);

  private final Predicate<Ship> enemy = new Predicate<Ship>() {
    @Override
    public boolean apply(Ship s) {
      return s.faction != ship.faction;
    }
  };

}
