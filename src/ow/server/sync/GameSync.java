package ow.server.sync;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ow.server.OWServer;
import ow.server.arch.SwapSet;
import ow.server.arch.qtree.Query;
import ow.server.model.Player;
import ow.server.model.Ship;
import ow.server.model.Shot;

/**
 * In charge of telling players when relevant entities have updated.
 */
public class GameSync {

  private static boolean DEBUG = false;

  private final OWServer server;

  private SwapSet<Ship> dirty = SwapSet.create();
  private SwapSet<Shot> shots = SwapSet.create();
  private SwapSet<ShotHit> shotsHit = SwapSet.create();

  public GameSync(OWServer server) {
    this.server = server;

    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(updater, 0, 10,
        TimeUnit.MILLISECONDS);
  }

  public void markUpdated(Ship ship) {
    dirty.add(ship);
  }

  public void onHit(Shot shot, Ship ship, double damage) {
    shotsHit.add(new ShotHit(shot, ship, damage));
  }

  public void onShotsFired(List<Shot> shots) {
    this.shots.addAll(shots);
  }

  private void sendUpdates(Set<Ship> updated, Set<Shot> shots, Set<ShotHit> shotsHit) {
    for (Player player : server.getPlayers()) {
      try {
        sendUpdates(updated, player, shots, shotsHit);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void sendUpdates(Set<Ship> updated, Player player, Set<Shot> shots, Set<ShotHit> shotsHit) {
    SyncInfo info = player.getSyncInfo();

    Ship playersShip = player.getShip();
    if (playersShip == null) {
      playersShip = player.getLastShip();
      if (playersShip == null) {
        return;
      }
    }

    Query query = Query.start(playersShip.x, playersShip.y).radius(2000);
    Set<Ship> nearby = Sets.newHashSet(server.getWorld().getShips().select(query));
    nearby.add(playersShip);

    Set<Ship> brandNewShips = Sets.difference(nearby, info.shipsSeen);
    Set<Ship> outOfRange = Sets.difference(info.shipsNearby, nearby);
    Set<Ship> newlyInRange =
        Sets.difference(Sets.difference(nearby, brandNewShips), info.shipsNearby);

    int numNew = 0, numOut = 0, numUpdate = 0, numShots = 0;
    
    JsonArray a = new JsonArray();
    // todo only send the shots that are near this player
    a.add(createShotsObject(shots));

    // todo only send the shots that are near this player
    for (ShotHit shotHit : shotsHit) {
      a.add(createShotHitObject(shotHit));
      numShots++;
    }

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

    if (DEBUG && a.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("Update: ").append(numNew).append(" new, ")
          .append(numOut + " out, " + numUpdate + " updated" + ", " + numShots + " shots");
      System.out.println(sb);
    }

      server.send(a.toString(), player.getConnection());

    info.shipsNearby = nearby;
    info.shipsSeen.addAll(nearby);
  }

  private final Runnable updater = new Runnable() {
    @Override
    public void run() {
      if (dirty.isEmpty() && shots.isEmpty() && shotsHit.isEmpty()) {
        return;
      }

      sendUpdates(dirty.get(), shots.get(), shotsHit.get());
    }
  };

  private JsonObject createShotHitObject(ShotHit shotHit) {
    JsonObject o = new JsonObject();
    o.addProperty("command", "hit");
    o.addProperty("shot", shotHit.shot.id);
    o.addProperty("ship", shotHit.hit.id);
    o.addProperty("damage", shotHit.damage);
    return o;
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
    o.addProperty("hp", ship.hp);
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

  public void sendShip(Player player) {
    server.send(createShipObject(player.getShip()).toString(), player.getConnection());
  }

}
