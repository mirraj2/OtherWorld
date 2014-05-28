package ow.client.img;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ImgTest {

  public void run() throws Exception {
    BufferedImage bi = ImageIO.read(new File("C:/shit/img/orange.jpg"));
    BufferedImage bi2 = setWidth(bi, 5000);
    System.out.println("Done resizing.");
    bi2 = oil(bi2);
    ImageIO.write(bi2, "jpg", new File("C:/shit/img/orange-big.jpg"));
  }

  private BufferedImage setWidth(BufferedImage bi, int width) {
    double m = 1.0 * width / bi.getWidth();
    int height = (int) (m * bi.getHeight());
    return resize(bi, width, height);
  }

  private BufferedImage oil(BufferedImage bi) {
    BufferedImage dest = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
    BufferedImage ret = new OilFilter().filter(bi, dest);
    return ret;
  }

  private BufferedImage resize(BufferedImage bi, int width, int height) {
    BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    Graphics2D g = ret.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

    g.drawImage(bi, 0, 0, width, height, null);

    g.dispose();

    return ret;
  }

  public static void main(String[] args) throws Exception {
    new ImgTest().run();
  }

}
