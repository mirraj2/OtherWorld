package ow.client;

import java.awt.Rectangle;
import java.util.Random;

import org.jason.SGraphics;

import com.google.common.base.Throwables;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class BackgroundRenderer {

  public static final int STAR_SECTOR_SIZE = 600;
  private static final int NUM_STARS = 1200;
  private static final Random rand = new Random();

  private final Image stars;

  public BackgroundRenderer() {
    stars = generateStars();
  }

  public void render(Rectangle cameraLocation, SGraphics g) {
    int w = cameraLocation.width;
    int h = cameraLocation.height;
    int backgroundX = cameraLocation.x / 2;
    int backgroundY = cameraLocation.y / 2;

    g.push();

    g.translate(backgroundX, backgroundY);

    int size = BackgroundRenderer.STAR_SECTOR_SIZE;

    int startI = (backgroundX / size);
    int startJ = (backgroundY / size);

    if (backgroundX < 0) {
      startI--;
    }
    if (backgroundY < 0) {
      startJ--;
    }

    for (int i = startI; i <= startI + w / size + 1; i++) {
      for (int j = startJ; j <= startJ + h / size + 1; j++) {
        int offsetX = i * size;
        int offsetY = j * size;

        g.draw(stars, offsetX, offsetY);
      }
    }

    g.pop();
  }

  public static Image generateStars() {
    try {
      Image ret = new Image(STAR_SECTOR_SIZE, STAR_SECTOR_SIZE);

      Graphics g = ret.getGraphics();

      Color c = new Color(Color.white);
      for (int i = 0; i < NUM_STARS; i++) {
        g.setColor(c);
        c.a = rand.nextFloat() + .2f;
        int x = rand.nextInt(STAR_SECTOR_SIZE);
        int y = rand.nextInt(STAR_SECTOR_SIZE);
        int size = rand.nextBoolean() ? 1 : 2;
        g.fillOval(x, y, size, size);
      }

      g.destroy();

      return ret;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

}
