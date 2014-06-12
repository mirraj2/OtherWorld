package ow.server.gen;

import ow.common.Faction;
import ow.common.ShipType;
import ow.server.ai.ExpandAI;
import ow.server.ai.ProtectAI;
import ow.server.ai.ShipSpawner;
import ow.server.model.Planet;
import ow.server.model.Ship;
import ow.server.model.World;

/**
 * A scenario where to factions are battling to take control of the universe.
 */
public class TwoFactionScenario extends Generator {

  public TwoFactionScenario(World world) {
    super(world);
  }

  @Override
  public void generate() {
    Planet startingPlanet = world.getStartingPlanet();

    Ship starterStation = world.addShip(new Ship(Faction.EXPLORERS, ShipType.STATION, startingPlanet.x + 300,
        startingPlanet.y + 200));
    world.addAI(new ShipSpawner(world, starterStation, ShipType.MINI, 20, .5));

    for (Planet planet : world.getPlanets()) {
      if (planet == startingPlanet) {
        continue;
      }

      generate(planet);
    }
  }

  private void generate(Planet planet) {
    double d = rand.nextDouble();

    Faction faction;

    if (d < .5) {
      faction = Faction.EXPLORERS;
      if (d < .25) {
        generateBase(planet, faction);
      } else {
        generateUnits(planet, faction);
      }
    } else if (d < 1.0) {
      faction = Faction.FEDERATION;
      if (d < .75) {
        generateBase(planet, faction);
      } else {
        generateUnits(planet, faction);
      }
    }
  }

  private void generateBase(Planet planet, Faction faction) {
    Ship station = world.addShip(new Ship(faction, ShipType.STATION, planet.x + random(400), planet.y + random(400)));
    world.addAI(new ExpandAI(world, getStationSpawner(world, station)));
  }
  
  private void generateUnits(Planet planet, Faction faction) {
    int numUnits = (int) (3 * rand.nextGaussian() + 5);
    numUnits = Math.max(numUnits, 2);

    for (int i = 0; i < numUnits; i++) {
      ShipType shipType;
      if (rand.nextDouble() < .3333) {
        shipType = ShipType.FIGHTER;
      } else {
        shipType = ShipType.MINI;
      }
      Ship ship = world.addShip(new Ship(faction, shipType, planet.x + random(400), planet.y + random(400)));
      world.addAI(new ProtectAI(world, ship, planet));
    }
  }

  private double random(double n) {
    return Math.random() * n - n / 2;
  }

  public static ShipSpawner getStationSpawner(World world, Ship station) {
    ShipType spawnType;
    if (rand.nextDouble() < .2) {
      spawnType = ShipType.FIGHTER;
    } else {
      spawnType = ShipType.MINI;
    }

    return new ShipSpawner(world, station, spawnType, 500, Math.random() * .5 + .01);
    // return new ShipSpawner(world, station, spawnType, 10 + rand.nextInt(32),
    // Math.random() * .5 + .01);
  }

}
