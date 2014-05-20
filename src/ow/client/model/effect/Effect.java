package ow.client.model.effect;

import ow.client.arch.SGraphics;

public abstract class Effect {

  public abstract void tick(int delta);

  public abstract void render(SGraphics g);
  
  public abstract boolean isFinished();
  
}
