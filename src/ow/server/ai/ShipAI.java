package ow.server.ai;

import java.util.List;

import com.google.common.base.Predicate;
import ow.server.arch.qtree.Query;
import ow.server.model.Ship;
import ow.server.model.World;

import static com.google.common.base.Preconditions.checkNotNull;

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

  // private List<Ship> getNearbyEnemies(int radius) {
  // List<Ship> nearbyShips = world.getShips().select(Query.start(ship.x,
  // ship.y).radius(radius));
  // List<Ship> enemies = Lists.newArrayList(filter(nearbyShips, enemy));
  //
  // Collections.sort(enemies, new Comparator<Ship>() {
  // @Override
  // public int compare(Ship a, Ship b) {
  // double diff = ship.distSquared(a) - ship.distSquared(b);
  // if (diff == 0) {
  // return a.id - b.id;
  // }
  // return (int) Math.signum(diff);
  // }
  // });
  //
  // return enemies;
  // }

  protected Ship getClosestEnemy(int radius) {
    List<Ship> nearbyShips = world.getShips().select(Query.start(ship.x, ship.y).radius(radius));

    double minD = -1;
    Ship ret = null;

    for (Ship ship : nearbyShips) {
      if (ship.faction == this.ship.faction) {
        continue;
      }
      double d = this.ship.distSquared(ship);
      if (ret == null || d < minD) {
        minD = d;
        ret = ship;
      }
    }

    return ret;
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
