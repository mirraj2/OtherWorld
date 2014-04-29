package ow.server;

import java.util.List;

import com.google.common.collect.Lists;

public class World {

  private List<Ship> ships = Lists.newArrayList();

  public void add(Ship ship) {
    this.ships.add(ship);
  }

  public void remove(Ship ship) {
    this.ships.remove(ship);
  }

}
