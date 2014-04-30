package ow.server;

public abstract class AI {

  protected final World world;

  public AI(World world) {
    this.world = world;
  }

  public abstract void tick(double millis);

}
