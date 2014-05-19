package ow.server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ow.client.ImageLoader;
import swinglib.GFrame;

public class HitBoxTest extends JPanel {

  private double rotation = Math.PI / 4;
  private BufferedImage im = ImageLoader.getImage("ships/Test.png");
  private BufferedImage im2;
  private int markX, markY;
  private JLabel label1, label2;

  public HitBoxTest() {
    super(new MigLayout());

    setBackground(Color.white);

    label1 = new JLabel(new ImageIcon(im)) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.red);
        g.fillOval(markX - 3, markY - 3, 7, 7);
      }
    };
    label1.setBorder(BorderFactory.createLineBorder(Color.black, 1));

    im2 = new BufferedImage(211, 211, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = im2.createGraphics();
    g.rotate(-rotation, im2.getWidth() / 2, im2.getHeight() / 2);
    g.drawImage(im, (im2.getWidth() - im.getWidth()) / 2, (im2.getHeight() - im.getHeight()) / 2,
        null);
    g.dispose();

    label2 = new JLabel(new ImageIcon(im2));
    label2.setBorder(BorderFactory.createLineBorder(Color.black, 1));

    add(label1, "width 201!, height 201!");
    add(label2, "width " + im2.getWidth() + "!, height " + im2.getHeight() + "!");

    label2.addMouseMotionListener(mouseListener);
  }

  private void handle(int x, int y) {

    x -= im2.getWidth() / 2;
    y -= im2.getHeight() / 2;

    System.out.println("\nSTART\n");

    System.out.println(x + ", " + y);

    double r = rotation;

    double xOffset = x * Math.cos(r) - y * Math.sin(r);
    double yOffset = y * Math.cos(r) + x * Math.sin(r);

    System.out.println(xOffset + ", " + yOffset);

    markX = (int) (label1.getWidth() / 2 + xOffset);
    markY = (int) (label1.getHeight() / 2 + yOffset);

    System.out.println(markX + ", " + markY);

    repaint();
  }

  final MouseAdapter mouseListener = new MouseAdapter() {
    public void mouseMoved(MouseEvent e) {
      handle(e.getX(), e.getY());
    };
  };

  public static void main(String[] args) {
    GFrame frame = new GFrame("Hit Box Test", new HitBoxTest());
    frame.setSize(500, 300);
  }

}
