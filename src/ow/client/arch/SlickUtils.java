package ow.client.arch;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.google.common.base.Throwables;

public class SlickUtils {

  public static Image deepCopy(Image img) {
    try {
      Image ret = new Image(img.getWidth() + 10, img.getHeight() + 10);
      Graphics g = ret.getGraphics();
      g.drawImage(img, 5, 5);
      g.destroy();
      return ret;
    } catch (SlickException e) {
      throw Throwables.propagate(e);
    }
  }

}
