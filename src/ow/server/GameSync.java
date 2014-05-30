package ow.server;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * In charge of telling players when relevant entities have updated.
 */
public class GameSync {

  private final OWServer server;
  private Set<Ship> dirty = Sets.newHashSet();
  private Set<Ship> buffer = Sets.newHashSet();

  public GameSync(OWServer server) {
    this.server = server;

    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(updater, 0, 10,
        TimeUnit.MILLISECONDS);
  }

  public void remove(Ship ship) {}

  private void sendUpdates(Set<Ship> updated) {
    for (Player player : server.getPlayers()) {
      sendUpdates(updated, player);
    }
  }

  private void sendUpdates(Set<Ship> updated, Player player) {
    SyncInfo info = player.getSyncInfo();

    Set<Ship> nearby = server.getWorld().getNearbyShips(player.getShip(), 2000);
    nearby.add(player.getShip());
    
    Set<Ship> brandNewShips = Sets.difference(nearby, info.shipsSeen);
    Set<Ship> outOfRange = Sets.difference(info.shipsNearby, nearby);
    Set<Ship> newlyInRange =
        Sets.difference(Sets.difference(nearby, brandNewShips), info.shipsNearby);

    int numNew = 0, numOut = 0, numUpdate = 0;
    
    JsonArray a = new JsonArray();
    for (Ship ship : brandNewShips) {
      a.add(createShipObject(ship));
      numNew++;
    }
    for(Ship ship : outOfRange){
      a.add(createShipOffscreen(ship));
      numOut++;
    }
    for (Ship ship : Sets.union(newlyInRange, Sets.intersection(nearby, updated))) {
      a.add(createShipUpdate(ship));
      numUpdate++;
    }

    if (a.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("Update: ").append(numNew).append(" new, ")
          .append(numOut + " out, " + numUpdate + " updated");
      System.out.println(sb);

      server.send(a.toString(), player.getConnection());
    }

    info.shipsNearby = nearby;
    info.shipsSeen.addAll(nearby);
  }

  private final Runnable updater = new Runnable() {
    @Override
    public void run() {
      if (dirty.isEmpty()) {
        return;
      }

      Set<Ship> temp = buffer;
      buffer = dirty;
      dirty = temp;

      sendUpdates(buffer);

      buffer.clear();
    }
  };

  public void markUpdated(Ship ship) {
      dirty.add(ship);
  }

  private JsonObject createShipUpdate(Ship ship) {
    JsonObject o = new JsonObject();
    o.addProperty("command", "update");
    o.addProperty("id", ship.id);
    o.addProperty("x", ship.x);
    o.addProperty("y", ship.y);
    o.addProperty("rotation", ship.rotation);
    o.addProperty("moving", ship.moving);
    o.addProperty("direction", ship.movementDirection);
    return o;
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
    o.addProperty("direction", ship.movementDirection);
    o.addProperty("moving", ship.moving);
    o.addProperty("max_hp", ship.maxHP);
    o.addProperty("hp", ship.hp);
    return o;
  }

  private JsonObject createShipOffscreen(Ship ship) {
    JsonObject o = new JsonObject();
    o.addProperty("command", "offscreen");
    o.addProperty("id", ship.id);
    return o;
  }

  public void onNewPlayer(Player player) {
    server.send(createShipObject(player.getShip()).toString(), player.getConnection());
  }

}
