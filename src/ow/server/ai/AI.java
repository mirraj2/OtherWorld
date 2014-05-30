package ow.server.ai;

import ow.server.World;

public abstract class AI {

  protected final World world;

  public AI(World world) {
    this.world = world;
  }

  /**
   * @return 'true' if this AI has completed and should no longer be ticked.
   */
  public abstract boolean tick(double millis);

}
