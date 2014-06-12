package ow.client.model.effect;

import org.jason.SGraphics;

public abstract class Effect {

  public abstract void tick(int delta);

  public abstract void render(SGraphics g);
  
  public abstract boolean isFinished();
  
}
