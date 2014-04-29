package ow.common;

import ow.server.Entity;

public class Planet extends Entity {

  public final String name;

  public Planet(String name, double x, double y) {
    this.name = name;
    this.x = x;
    this.y = y;
  }

}
