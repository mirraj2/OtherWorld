package ow.server.ai;

import java.util.concurrent.TimeUnit;

import ow.common.OMath;
import ow.server.model.Entity;
import ow.server.model.Ship;
import ow.server.model.World;

public class ProtectAI extends ShipAI {

  private static final double PATROL_DISTANCE = 400;

  private final Entity toProtect;
  private final Task findTargetToDestroy = Task.every(1, TimeUnit.SECONDS);
  private final Task fire = Task.every(1, TimeUnit.SECONDS);

  // ship we're trying to destroy
  private Ship target;

  private boolean returning = false;

  private boolean hasPatrolTarget = false;
  private double targetX, targetY;

  public ProtectAI(World world, Ship ship, Entity toProtect) {
    super(world, ship);
    this.toProtect = toProtect;
  }

  @Override
  public boolean run(double millis) {
    if (returning) {
      if (ship.distSquared(toProtect) < PATROL_DISTANCE * PATROL_DISTANCE) {
        returning = false;
      }
      return false;
    }

    if (target != null && target.isDead()) {
      target = null;
      findTargetToDestroy.reset();
    }

    if (findTargetToDestroy.isReady()) {
      target = getClosestEnemy(1000);
      if (target != null) {
        hasPatrolTarget = false;
      }
    }
    
    if (target == null) {
      patrol(millis);
    } else {
      if (ship.distSquared(toProtect) > PATROL_DISTANCE * 4 * PATROL_DISTANCE * 4) {
        // stop pursuing if we get too far away from what we are protecting
        returnToProtect();
      } else {
        pursueTarget(millis);
      }
    }

    return false;
  }
  
  private void returnToProtect() {
    target = null;
    returning = true;

    double r = OMath.getTargetRotation(ship.x, ship.y, toProtect.x, toProtect.y);
    ship.rotation(r).moving(true);
    world.sendUpdate(ship);
  }

  // just fly around the ship we're protecting.
  private void patrol(double millis) {
    if (hasPatrolTarget) {
      if (ship.distSquared(targetX, targetY) < 400) {
        goToNextPatrolLocation();
      } else {
        continueMoving(millis);
      }
    } else {
      goToNextPatrolLocation();
    }
  }

  private void continueMoving(double millis) {
    double oldR = ship.rotation;

    double d = ship.rotateTo(targetX, targetY, millis);
    ship.moving(d < Math.PI / 3);

    if (oldR != ship.rotation) {
      world.sendUpdate(ship);
    }
  }

  private void goToNextPatrolLocation() {
    targetX = toProtect.x + Math.random() * PATROL_DISTANCE * 2 - PATROL_DISTANCE;
    targetY = toProtect.y + Math.random() * PATROL_DISTANCE * 2 - PATROL_DISTANCE;

    hasPatrolTarget = true;
  }

  private void pursueTarget(double millis) {
    double rBefore = ship.rotation;
    boolean movingBefore = ship.moving;

    double rLeft = ship.rotateTo(target, millis);
    double distanceToTarget = ship.distSquared(target);
    double moveToDistance = 200 * 200;
    double shootDistance = 400 * 400;

    ship.moving(distanceToTarget > moveToDistance && rLeft < Math.PI / 6);

    if (distanceToTarget < shootDistance && rLeft < Math.PI / 12) {
      if (fire.isReady()) {
        world.fire(ship);
      }
    }

    if (ship.rotation != rBefore || ship.moving != movingBefore) {
      world.sendUpdate(ship);
    }
  }

}
