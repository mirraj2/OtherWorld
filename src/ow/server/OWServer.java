package ow.server;

import java.util.Collection;
import java.util.Map;

import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.Server;
import jexxus.server.ServerConnection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ow.common.Faction;
import ow.common.ShipType;
import ow.server.model.Planet;
import ow.server.model.Player;
import ow.server.model.Ship;
import ow.server.model.Shot;
import ow.server.model.World;
import ow.server.sync.GameSync;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class OWServer implements ConnectionListener {

  private static final Logger logger = Logger.getLogger(OWServer.class);

  public static boolean FAST_SPAWN = false;
  private static final ShipType STARTING_SHIP = ShipType.CHEATSHIP;
  private static final int PORT = 19883;
  private static final JsonParser parser = new JsonParser();

  private final Map<Connection, Player> connectionPlayers = Maps.newConcurrentMap();
  private final GameSync sync = new GameSync(this);
  private final World world = new World(this);

  private void sendFullUpdate(Player player) {
    for (Planet planet : world.getPlanets()) {
      send(createPlanetObject(planet), player.getConnection());
    }
  }

  public void onHit(Shot shot, Ship ship, double damage) {
    if (ship.isDead()) {
      for (Player p : connectionPlayers.values()) {
        if (p.getShip() == ship) {
          p.setShip(null);
        }
      }
    }

    sync.onHit(shot, ship, damage);
  }

  public void onShotsFired(Collection<Shot> shots) {
    sendToAll(createShotsObject(shots));
  }

  private void sendToAllBut(JsonElement o, Connection sender) {
    String s = o.toString();
    for (Connection c : connectionPlayers.keySet()) {
      if (c != sender) {
        send(s, c);
      }
    }
  }

  public void sendToAll(JsonElement o) {
    sendToAllBut(o, null);
  }

  private JsonObject createPlanetObject(Planet planet) {
    JsonObject o = new JsonObject();
    o.addProperty("id", planet.id);
    o.addProperty("command", "planet");
    o.addProperty("name", planet.name);
    o.addProperty("x", planet.x);
    o.addProperty("y", planet.y);
    o.addProperty("color", planet.color);

    JsonArray connections = new JsonArray();
    for (Planet p : planet.connections) {
      connections.add(new JsonPrimitive(p.id));
    }
    o.add("connections", connections);

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
    Player player = connectionPlayers.remove(broken);
    if (player.getShip() != null) {
      world.removeShip(player.getShip());
    }
  }

  @Override
  public void receive(byte[] data, Connection from) {
    JsonObject o = parser.parse(new String(data, Charsets.UTF_8)).getAsJsonObject();
    String command = o.get("command").getAsString();

    Player player = connectionPlayers.get(from);
    Ship ship = player.getShip();

    if (command.equals("update")) {
      checkArgument(Objects.equal(ship.id, o.get("id").getAsInt()),
          "Cannot update a ship that is not your own! " + ship.id + " vs " + o.get("id"));

      ship.x = o.get("x").getAsDouble();
      ship.y = o.get("y").getAsDouble();
      ship.moving = o.get("moving").getAsBoolean();
      ship.rotation = o.get("rotation").getAsDouble();
      ship.movementDirection = o.get("direction").getAsDouble();

      sendToAllBut(o, from);
    } else if (command.equals("shoot")) {
      world.fire(ship);
    } else if (command.equals("respawn")) {
      checkState(ship == null, "Cannot respawn when you already have a ship!");
      
      spawn(player);
    }
    else {
      logger.debug("Unknown message: " + o);
    }
  }

  private void spawn(Player p) {
    Ship ship = new Ship(Faction.EXPLORERS, STARTING_SHIP, world.getPlayerSpawnLocation());
    p.setShip(ship);
    world.addShip(ship);

    sync.sendShip(p);

    JsonObject o = new JsonObject();
    o.addProperty("command", "take_control");
    o.addProperty("id", ship.id);
    send(o, p.getConnection());
  }

  @Override
  public void clientConnected(ServerConnection conn) {
    logger.debug("Client connected: " + conn);

    Player player = new Player(conn);
    connectionPlayers.put(conn, player);
    sendFullUpdate(player);

    spawn(player);
  }

  private void send(JsonObject o, Connection conn) {
    send(o.toString(), conn);
  }

  public void send(String s, Connection conn) {
    try {
      conn.send(s.getBytes(Charsets.UTF_8), Delivery.RELIABLE);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Iterable<Player> getPlayers() {
    return connectionPlayers.values();
  }

  public GameSync getSync() {
    return sync;
  }

  public World getWorld() {
    return world;
  }

  public static void main(String[] args) {
    BasicConfigurator.configure();

    logger.info("Starting server...");

    new Server(new OWServer(), PORT, false).startServer();

    logger.info("Server started!");
  }

}
