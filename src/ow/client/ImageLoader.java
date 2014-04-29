package ow.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class ImageLoader {

  private static final Map<String, BufferedImage> cache = Maps.newHashMap();
  private static final Map<String, Image> cache2 = Maps.newHashMap();

  public static BufferedImage getImage(String s) {
    BufferedImage ret = cache.get(s);
    if (ret == null) {
      try {
        ret = ImageIO.read(ImageLoader.class.getResource("rez/" + s));
        cache.put(s, ret);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
    return ret;
  }

  public static Image getSlickImage(String s) {
    Image ret = cache2.get(s);
    if (ret == null) {
      InputStream in = ImageLoader.class.getResourceAsStream("rez/" + s);
      try {
        ret = new Image(in, s, true);
        cache2.put(s, ret);
      } catch (SlickException e) {
        throw Throwables.propagate(e);
      }
    }
    return ret;
  }

}
