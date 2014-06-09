package ow.server.ai;

import java.util.concurrent.TimeUnit;

import ow.common.OMath;
import ow.server.arch.Task;
import ow.server.model.Entity;
import ow.server.model.Ship;
import ow.server.model.World;

public class ProtectAI extends ShipAI {

  private Entity toProtect;
  private double patrolDistance = 400;
  private final Task findTargetToDestroy = Task.every(1, TimeUnit.SECONDS);
  private final Task fire = Task.every(1, TimeUnit.SECONDS);

  // ship we're trying to destroy
  private Ship target;

  private boolean returning = false;

  private boolean hasPatrolTarget = false;
  private double targetX, targetY;
  private boolean doneRotating = false;

  public ProtectAI(World world, Ship ship, Entity toProtect) {
    super(world, ship);
    this.toProtect = toProtect;
  }

  public ProtectAI protect(Entity toProtect) {
    if (this.toProtect != toProtect) {
      this.toProtect = toProtect;
      returning = false;
      hasPatrolTarget = false;
    }
    return this;
  }

  public ProtectAI patrolDistance(double patrolDistance) {
    this.patrolDistance = patrolDistance;
    return this;
  }

  @Override
  public boolean run(double millis) {
    if (returning) {
      if (ship.distSquared(toProtect) < patrolDistance * patrolDistance) {
        returning = false;
      }
    }

    if (target != null && target.isDead()) {
      target = null;
      findTargetToDestroy.reset();
    }

    if (!returning && findTargetToDestroy.isReady()) {
      target = getClosestEnemy(1000);
      if (target != null) {
        hasPatrolTarget = false;
      }
    }
    
    if (target == null) {
      patrol(millis);
    } else {
      if (ship.distSquared(toProtect) > patrolDistance * 4 * patrolDistance * 4) {
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
    targetX = toProtect.x;
    targetY = toProtect.y;
    doneRotating = false;
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

    ship.moving(true);

    if (!doneRotating) { // optimization
      double rotationLeft = ship.rotateTo(targetX, targetY, millis, true);
      if (OMath.isZero(rotationLeft)) {
        doneRotating = true;
      }
    }

    if (!OMath.equals(oldR, ship.rotation)) {
      world.sendUpdate(ship);
    }
  }

  private void goToNextPatrolLocation() {
    targetX = toProtect.x + Math.random() * patrolDistance * 2 - patrolDistance;
    targetY = toProtect.y + Math.random() * patrolDistance * 2 - patrolDistance;
    doneRotating = false;

    hasPatrolTarget = true;
    ship.moving(true);
    world.sendUpdate(ship);
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
