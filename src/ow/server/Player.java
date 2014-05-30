package ow.server;

import jexxus.common.Connection;

public class Player {

  private final Connection connection;
  private final Ship ship;

  private final SyncInfo syncInfo = new SyncInfo();

  public Player(Connection connection, Ship ship) {
    this.connection = connection;
    this.ship = ship;
  }

  public Connection getConnection() {
    return connection;
  }

  public Ship getShip() {
    return ship;
  }

  public SyncInfo getSyncInfo() {
    return syncInfo;
  }

}
