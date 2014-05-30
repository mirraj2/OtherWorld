package ow.server.sync;

import ow.server.model.Ship;
import ow.server.model.Shot;

public class ShotHit {

  public final Shot shot;
  public final Ship hit;
  public final double damage;

  public ShotHit(Shot shot, Ship hit, double damage) {
    this.shot = shot;
    this.hit = hit;
    this.damage = damage;
  }

}
