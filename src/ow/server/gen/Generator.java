package ow.server.gen;

import java.util.Random;

import ow.server.model.World;

public abstract class Generator {

  protected static final Random rand = new Random();

  protected final World world;

  public Generator(World world) {
    this.world = world;
  }

  public abstract void generate();

}
