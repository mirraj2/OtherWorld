package ow.server.model;

import ow.server.sync.SyncInfo;
import jexxus.common.Connection;

public class Player {

  private final Connection connection;
  private Ship ship;
  private Ship lastShip;

  private final SyncInfo syncInfo = new SyncInfo();

  public Player(Connection connection) {
    this.connection = connection;
  }

  public Connection getConnection() {
    return connection;
  }

  public Ship getShip() {
    return ship;
  }

  public void setShip(Ship ship) {
    this.lastShip = this.ship;
    this.ship = ship;
  }

  public SyncInfo getSyncInfo() {
    return syncInfo;
  }

  public Ship getLastShip() {
    return lastShip;
  }

}
