package ow.client;

import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.server.ServerConnection;

import org.apache.log4j.Logger;

import ow.client.model.ClientModel;
import ow.client.model.Planet;
import ow.client.model.Ship;
import ow.client.model.Shot;
import ow.common.Faction;
import ow.common.ShipType;

import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.google.common.base.Preconditions.checkNotNull;

public class NetworkHandler implements ConnectionListener {

  private static final Logger logger = Logger.getLogger(NetworkHandler.class);

  private static final JsonParser parser = new JsonParser();

  private final OWClient client;
  private final ClientModel model;

  public NetworkHandler(OWClient client, ClientModel model) {
    this.client = client;
    this.model = model;
  }

  @Override
  public void receive(byte[] data, Connection from) {
    JsonElement e = parser.parse(new String(data, Charsets.UTF_8));

    if (e instanceof JsonArray) {
      for (JsonElement part : e.getAsJsonArray()) {
        handle(part.getAsJsonObject());
      }
    } else {
      handle(e.getAsJsonObject());
    }
  }

  private void handle(JsonObject o) {
    String command = o.get("command").getAsString().toLowerCase();

    if (command.equals("ship")) {
      int id = o.get("id").getAsInt();
      double x = o.get("x").getAsDouble();
      double y = o.get("y").getAsDouble();
      double rotation = o.get("rotation").getAsDouble();
      double maxHP = o.get("max_hp").getAsDouble();
      double hp = o.get("hp").getAsDouble();
      boolean moving = o.get("moving").getAsBoolean();
      double movementDirection = o.get("direction").getAsDouble();

      Ship ship = new Ship(id, Faction.valueOf(o.get("faction").getAsString()),
          ShipType.valueOf(o.get("type").getAsString()), hp, maxHP)
              .setLocation(x, y).setRotation(rotation).moving(moving)
              .movementDirection(movementDirection);
      model.addShip(ship);
    } else if (command.equals("planet")) {
      model.addPlanet(new Planet(o.get("id").getAsInt(), o.get("name").getAsString(), o
          .get("color").getAsInt(), o.get("x").getAsDouble(), o.get("y").getAsDouble(),
          o.get("connections").getAsJsonArray()));
    } else if (command.equals("update")) {
      int id = o.get("id").getAsInt();
      Ship ship = model.getShip(id);
      if (ship == null) {
        logger.warn("Don't know about ship: " + id);
        return;
      }
      ship.x = o.get("x").getAsDouble();
      ship.y = o.get("y").getAsDouble();
      ship.rotation = o.get("rotation").getAsDouble();
      ship.moving = o.get("moving").getAsBoolean();
      ship.active = true;
    } else if (command.equals("shots")) {
      for (JsonElement e : o.getAsJsonArray("shots")) {
        JsonObject s = e.getAsJsonObject();
        model.addShot(new Shot(s.get("id").getAsInt(), s.get("x").getAsDouble(), s.get("y")
            .getAsDouble(), s.get("rotation").getAsDouble(), s.get("velocity").getAsDouble(), s
            .get("max_distance").getAsDouble()));
      }
    } else if (command.equals("hit")) {
      int shotID = o.get("shot").getAsInt();
      int shipID = o.get("ship").getAsInt();
      double damage = o.get("damage").getAsDouble();

      Ship ship = model.getShip(shipID);
      ship.hp = Math.max(0, ship.hp - damage);

      if (ship.hp == 0) {
        model.explodeShip(ship);
        if (ship == client.getMyShip()) {
          client.playerDied();
        }
      }

      model.removeShot(shotID);
    }
    else if (command.equals("offscreen")) {
      int id = o.get("id").getAsInt();
      Ship ship = model.getShip(id);
      if (ship != null) {
        ship.active = false;
      }
    } else if (command.equals("take_control")) {
      int id = o.get("id").getAsInt();
      Ship ship = model.getShip(id);

      checkNotNull(ship);

      client.setMyShip(ship);
      model.focus(ship);
    }
    else {
      logger.warn("unknown message: " + o);
    }
  }

  @Override
  public void clientConnected(ServerConnection conn) {}

  @Override
  public void connectionBroken(Connection broken, boolean forced) {
    logger.info("Lost connection to the server.");
  }

}
