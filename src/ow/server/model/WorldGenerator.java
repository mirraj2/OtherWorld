package ow.server.model;

import java.util.Random;

import ow.common.Faction;
import ow.common.ShipType;
import ow.server.ai.ExpandAI;
import ow.server.ai.ProtectAI;
import ow.server.ai.ShipSpawner;

public class WorldGenerator {

  private final Random rand = new Random();

  private final World world;

  public WorldGenerator(World world) {
    this.world = world;
  }

  public void generate() {
    Planet startingPlanet = world.getStartingPlanet();
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
    } else {
      // neutral planet
    }
  }

  private void generateBase(Planet planet, Faction faction) {
    ShipType spawnType;
    if (rand.nextDouble() < .2) {
      spawnType = ShipType.FIGHTER;
    } else {
      spawnType = ShipType.MINI;
    }

    Ship station = world.add(new Ship(faction, ShipType.STATION, planet.x + random(400), planet.y + random(400)));
    ShipSpawner spawner = new ShipSpawner(world, station, spawnType, 2 + rand.nextInt(11), Math.random() + .01);
    world.addAI(new ExpandAI(world, spawner));
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
      Ship ship = world.add(new Ship(faction, shipType, planet.x + random(400), planet.y + random(400)));
      world.addAI(new ProtectAI(world, ship, planet));
    }
  }

  private double random(double n) {
    return Math.random() * n - n / 2;
  }

}
