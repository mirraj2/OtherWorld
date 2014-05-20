package ow.client.model.effect;

import java.util.Arrays;

import ow.client.arch.SBuffer;

import ow.client.arch.SGraphics;
import ow.client.arch.SlickUtils;

import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import ow.client.model.Ship;

public class ShipExplosion extends Effect {

  public ShipExplosion(Ship ship) {
    initialize(ship);
  }

  private void initialize(Ship ship) {
    Image im = SlickUtils.deepCopy(ship.getImage());
    SGraphics g = SGraphics.create(im);

    int numVerticalLines = 3;
    int numHorizontalLines = 2;
    int w = im.getWidth();
    int h = im.getHeight();

    SBuffer buffer = 
    
    byte[] textureData = im.getTexture().getTextureData();
    
    new ImageBuffer(im.getWidth(), im.getHeight());

    System.out.println(Arrays.toString(textureData));

    for (int i = 0; i < numVerticalLines; i++) {
      clearHorizontalLine(w, h, g, (int) (h / (numVerticalLines + 1.0)));
    }
    for (int i = 0; i < numHorizontalLines; i++) {

    }

    g.destroy();
  }

  private void clearHorizontalLine(int w, int h, SGraphics g, int y) {
  }

  @Override
  public void tick(int delta) {}

  @Override
  public void render(SGraphics g) {

  }

  @Override
  public boolean isFinished() {
    return false;
  }

}
