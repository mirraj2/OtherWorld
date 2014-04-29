package ow.server;

import java.awt.Point;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.Server;
import jexxus.server.ServerConnection;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class OWServer implements ConnectionListener {

  private static final Logger logger = Logger.getLogger(OWServer.class);

  private static final int PORT = 19883;
  private static final Point SPAWN_LOCATION = new Point(10000, 10000);

  private final World world = new World();

  private final Map<Connection, Ship> connectionShips = Maps.newConcurrentMap();

  @Override
  public void connectionBroken(Connection broken, boolean forced) {
    logger.debug("Lost connection with: " + broken);
    Ship ship = connectionShips.remove(broken);
    world.remove(ship);
  }

  @Override
  public void receive(byte[] data, Connection from) {
    logger.debug("received " + data.length + " bytes of data from " + from);
  }

  @Override
  public void clientConnected(ServerConnection conn) {
    logger.debug("Client connected: " + conn);

    Ship ship = new Ship(SPAWN_LOCATION);
    world.add(ship);

    JsonObject o = new JsonObject();
    o.addProperty("command", "spawn");
    o.addProperty("x", ship.getX());
    o.addProperty("y", ship.getY());
    
    conn.send(o.toString().getBytes(Charsets.UTF_8), Delivery.RELIABLE);
  }

  public static void main(String[] args) {
    BasicConfigurator.configure();

    logger.info("Starting server...");

    new Server(new OWServer(), PORT, false).startServer();

    logger.info("Server started!");
  }

}
