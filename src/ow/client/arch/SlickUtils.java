package ow.client.arch;

import com.google.common.base.Throwables;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class SlickUtils {

  public static Image deepCopy(Image img) {
    try {
      Image ret = new Image(img.getWidth(), img.getHeight());
      Graphics g = ret.getGraphics();
      g.drawImage(img, 0, 0);
      g.destroy();
      return ret;
    } catch (SlickException e) {
      throw Throwables.propagate(e);
    }
  }

}
