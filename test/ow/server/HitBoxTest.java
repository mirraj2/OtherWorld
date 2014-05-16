package ow.server;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ow.client.ImageLoader;
import swinglib.GFrame;

public class HitBoxTest extends JPanel {

  private double rotation = Math.PI / 20;
  private BufferedImage im = ImageLoader.getImage("ships/Test.png");
  private BufferedImage im2;

  public HitBoxTest() {
    super(new MigLayout());

    setBackground(Color.white);

    JLabel label1 = new JLabel(new ImageIcon(im));
    label1.setBorder(BorderFactory.createLineBorder(Color.black, 1));

    im2 = new BufferedImage(211, 211, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = im2.createGraphics();
    g.rotate(rotation, im2.getWidth() / 2, im2.getHeight() / 2);
    g.drawImage(im, (im2.getWidth() - im.getWidth()) / 2, (im2.getHeight() - im.getHeight()) / 2,
        null);
    g.dispose();

    JLabel label2 = new JLabel(new ImageIcon(im2));
    label2.setBorder(BorderFactory.createLineBorder(Color.black, 1));

    add(label1, "width 201!, height 201!");
    add(label2, "width " + im2.getWidth() + "!, height " + im2.getHeight() + "!");
  }

  public static void main(String[] args) {
    GFrame frame = new GFrame("Hit Box Test", new HitBoxTest());
    frame.setSize(500, 300);
  }

}
