package ow.server.brain;

import ow.common.OMath;
import ow.server.AI;
import ow.server.Ship;
import ow.server.World;

public class ProtectAI extends AI {

  private static final double PATROL_DISTANCE = 300;
  private static final double PATROL_CYCLE = 2000;

  private final Ship ship;
  private final Ship toProtect;

  private double timeUntilNextPatrol = PATROL_CYCLE;

  public ProtectAI(World world, Ship ship, Ship toProtect) {
    super(world);
    this.ship = ship;
    this.toProtect = toProtect;

    patrol();
  }

  private void patrol() {
    double targetX = toProtect.x + Math.random() * PATROL_DISTANCE * 2 - PATROL_DISTANCE;
    double targetY = toProtect.y + Math.random() * PATROL_DISTANCE * 2 - PATROL_DISTANCE;

    double r = OMath.getTargetRotation(ship.x, ship.y, targetX, targetY);
    ship.rotation(r).moving(true);
    world.sendUpdate(ship);
  }

  @Override
  public void tick(double millis) {
    // for now just fly around the ship we're protecting.
    timeUntilNextPatrol -= millis;
    if (timeUntilNextPatrol <= 0) {
      timeUntilNextPatrol = PATROL_CYCLE;
      patrol();
    }
  }

}
