package ow.server;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collection;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import ow.client.ImageLoader;
import ow.common.Faction;
import ow.common.OMath;
import ow.common.ShipType;

public class Ship extends Entity {

  public final ShipType type;
  public final Faction faction;
  public final double maxHP;
  public final double maxSpeed;
  public final Collection<Point> gunLocations;
  public final BufferedImage image;

  public boolean moving = false;
  public double rotation = Math.PI / 2;
  public double movementDirection = 0;
  public double hp;

  public Ship(Faction faction, ShipType type, Point location) {
    this(faction, type, location.x, location.y);
  }

  public Ship(Faction faction, ShipType type, double x, double y) {
    this.faction = faction;
    this.type = type;
    this.x = x;
    this.y = y;
    this.maxSpeed = type.getMaxSpeed();
    this.gunLocations = type.getGunLocations();
    this.image = ImageLoader.getImage("ships/" + type.getImageName());
    this.maxHP = hp = type.getHP();
  }

  public void tick(double millis) {
    if (moving) {
      moveForward(millis);
    }
  }

  private void moveForward(double millis) {
    double r = rotation + movementDirection;

    x += (float) (Math.cos(r) * millis * maxSpeed / 1000);
    y -= (float) (Math.sin(r) * millis * maxSpeed / 1000);
  }

  public Ship rotation(double rotation) {
    rotation %= OMath.TWO_PI;
    if (rotation < 0) {
      rotation += OMath.TWO_PI;
    }
    this.rotation = rotation;
    return this;
  }

  public Ship moving(boolean moving) {
    this.moving = moving;
    return this;
  }

  public Shape getCollisionArea() {
    int size = Math.max(image.getWidth(), image.getHeight());
    return new Rectangle((float) (x - size / 2), (float) (y - size / 2), size, size);
  }

  public boolean isDead() {
    return hp <= 0;
  }

  public boolean isAlive() {
    return hp > 0;
  }

  /**
   * Returns the amount left to rotate.
   */
  public double rotateTo(Entity target, double millis) {
    double targetR = OMath.getTargetRotation(x, y, target.x, target.y);

    if (OMath.equals(rotation, targetR)) {
      return 0;
    }

    rotateTo(targetR, millis);

    return Math.abs(targetR - rotation) % Math.PI;
  }

  private void rotateTo(double targetR, double millis) {
    double turnAmount = type.getTurnSpeed() * millis;

    if (Math.abs(rotation - targetR) < turnAmount) {
      rotation(targetR);
      return;
    }

    boolean turnLeft;
    if (rotation < targetR) {
      turnLeft = targetR - rotation < Math.PI;
    } else {
      turnLeft = rotation - targetR >= Math.PI;
    }

    if (turnLeft) {
      rotation(rotation + turnAmount);
    } else {
      rotation(rotation - turnAmount);
    }
  }

}
