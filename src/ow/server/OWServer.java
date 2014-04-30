package ow.server;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.Server;
import jexxus.server.ServerConnection;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import ow.common.Faction;
import ow.common.ShipType;

import static com.google.common.base.Preconditions.checkArgument;

public class OWServer implements ConnectionListener {

  private static final Logger logger = Logger.getLogger(OWServer.class);

  private static final int PORT = 19883;
  private static final JsonParser parser = new JsonParser();

  private final World world = new World();

  private final Map<Connection, Ship> connectionShips = Maps.newConcurrentMap();

  private void sendFullUpdate(ServerConnection conn) {
    Ship clientShip = connectionShips.get(conn);

    for (Ship ship : world.getShips()) {
      JsonObject o = createShipObject(ship);
      if (ship == clientShip) {
        o.addProperty("control", true);
      }
      send(o, conn);
    }

    for (Planet planet : world.getPlanets()) {
      send(createPlanetObject(planet), conn);
    }
  }

  private void onShipAdded(Ship ship) {
    sendToAll(createShipObject(ship));
  }

  private void sendToAllBut(JsonObject o, Connection sender) {
    String s = o.toString();
    for (Connection c : connectionShips.keySet()) {
      if (c != sender) {
        send(s, c);
      }
    }
  }

  private void sendToAll(JsonObject o) {
    sendToAllBut(o, null);
  }

  private JsonObject createShipObject(Ship ship) {
    JsonObject o = new JsonObject();
    o.addProperty("command", "ship");
    o.addProperty("id", ship.id);
    o.addProperty("faction", ship.faction.name());
    o.addProperty("type", ship.type.name());
    o.addProperty("x", ship.x);
    o.addProperty("y", ship.y);
    o.addProperty("rotation", ship.rotation);
    o.addProperty("moving", ship.moving);
    return o;
  }

  private JsonObject createPlanetObject(Planet planet) {
    JsonObject o = new JsonObject();
    o.addProperty("command", "planet");
    o.addProperty("name", planet.name);
    o.addProperty("x", planet.x);
    o.addProperty("y", planet.y);
    return o;
  }

  private JsonObject createShotsObject(Collection<Shot> shots) {
    JsonObject o = new JsonObject();
    o.addProperty("command", "shots");
    JsonArray shotsArray = new JsonArray();
    for (Shot shot : shots) {
      JsonObject s = new JsonObject();
      s.addProperty("id", shot.id);
      s.addProperty("x", shot.x);
      s.addProperty("y", shot.y);
      s.addProperty("rotation", shot.rotation);
      s.addProperty("velocity", shot.velocity);
      s.addProperty("max_distance", shot.maxDistance);
      shotsArray.add(s);
    }
    o.add("shots", shotsArray);
    return o;
  }

  @Override
  public void connectionBroken(Connection broken, boolean forced) {
    logger.debug("Lost connection with: " + broken);
    Ship ship = connectionShips.remove(broken);
    world.remove(ship);
  }

  @Override
  public void receive(byte[] data, Connection from) {
    JsonObject o = parser.parse(new String(data, Charsets.UTF_8)).getAsJsonObject();
    String command = o.get("command").getAsString();

    Ship ship = connectionShips.get(from);

    if (command.equals("update")) {
      checkArgument(Objects.equal(ship.id, o.get("id").getAsInt()),
          "Cannot update a ship that is not your own! " + ship.id + " vs " + o.get("id"));

      ship.x = o.get("x").getAsDouble();
      ship.y = o.get("y").getAsDouble();
      ship.moving = o.get("moving").getAsBoolean();
      ship.rotation = o.get("rotation").getAsDouble();

      sendToAllBut(o, from);
    } else if (command.equals("shoot")) {
      sendToAll(createShotsObject(world.fire(ship)));
    }
    else {
      logger.debug("Unknown message: " + o);
    }
  }

  @Override
  public void clientConnected(ServerConnection conn) {
    logger.debug("Client connected: " + conn);

    Ship clientShip = new Ship(Faction.EXPLORERS, ShipType.MINI, world.getPlayerSpawnLocation());
    world.add(clientShip);
    connectionShips.put(conn, clientShip);
    sendFullUpdate(conn);
    onShipAdded(clientShip);
  }

  private void send(JsonObject o, Connection conn) {
    send(o.toString(), conn);
  }

  private void send(String s, Connection conn) {
    conn.send(s.getBytes(Charsets.UTF_8), Delivery.RELIABLE);
  }

  public static void main(String[] args) {
    BasicConfigurator.configure();

    logger.info("Starting server...");

    new Server(new OWServer(), PORT, false).startServer();

    logger.info("Server started!");
  }

}
