package ow.client.model;

import org.jason.ImageLoader;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import ow.common.Faction;
import ow.common.OMath;
import ow.common.ShipType;

public class Ship {

  public final int id;
  public double x, y;
  public double rotation;
  public boolean moving = false;
  public double movementDirection = 0;
  public double hp;

  public final Faction faction;
  public final ShipType type;
  public final double maxSpeed, maxHP;

  private Image image;

  /**
   * Whether this ship is an active part of our model.
   */
  public boolean active = true;

  public Ship(int id, Faction faction, ShipType type, double hp, double maxHP) {
    this.id = id;
    this.faction = faction;
    this.type = type;
    this.maxSpeed = type.getMaxSpeed();
    this.hp = hp;
    this.maxHP = maxHP;
  }

  public Ship rotateToTarget(double targetX, double targetY) {
    rotation = OMath.getTargetRotation(x, y, targetX, targetY);
    return this;
  }

  public Ship setLocation(double x, double y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public Ship setRotation(double rotation) {
    this.rotation = rotation;
    return this;
  }

  public Ship movementDirection(double movementDirection) {
    this.movementDirection = movementDirection;
    return this;
  }

  public Ship moving(boolean moving) {
    this.moving = moving;
    return this;
  }

  public Ship halt() {
    moving = false;
    return this;
  }

  public void tick(int delta) {
    if (!active) {
      return;
    }

    if (type == ShipType.STATION) {
      rotation += 2 * Math.PI * delta / 1000 / 100;
    }

    if (moving) {
      moveForward(delta);
    }
  }

  private void moveForward(int delta) {
    double r = rotation + movementDirection;
    float dx = (float) (Math.cos(r) * delta * maxSpeed / 1000);
    float dy = -(float) (Math.sin(r) * delta * maxSpeed / 1000);

    x += dx;
    y += dy;
  }

  public Image getImage() {
    if (image == null) {
      image = ImageLoader.getSlickImage("ships/" + type.getImageName());
    }
    Color c = faction.getColor();
    image.setImageColor(c.r, c.g, c.b);
    image.setRotation(-(float) Math.toDegrees(rotation));
    return image;
  }

}
