package ow.server.ai;

import ow.server.model.Ship;
import ow.server.model.World;

import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;

public abstract class ShipAI extends AI {

  protected final Ship ship;

  public ShipAI(World world, Ship ship) {
    super(world);

    this.ship = checkNotNull(ship);
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

  public Ship getShip() {
    return ship;
  }

  private final Predicate<Ship> enemy = new Predicate<Ship>() {
    @Override
    public boolean apply(Ship s) {
      return s.faction != ship.faction;
    }
  };

}
