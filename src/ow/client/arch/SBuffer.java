package ow.client.arch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.BufferUtils;
import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.ImageData;

public class SBuffer implements ImageData {

  private int width, height, texWidth, texHeight;
  private byte[] rawData;

  public SBuffer(Image image) {
    width = image.getWidth();
    height = image.getHeight();
    texWidth = image.getTexture().getTextureWidth();
    texHeight = image.getTexture().getTextureHeight();
    rawData = image.getTexture().getTextureData();
  }

  public byte[] getRGBA() {
    return rawData;
  }

  public int getDepth() {
    return 32;
  }

  public int getHeight() {
    return height;
  }

  public int getTexHeight() {
    return texHeight;
  }

  public int getTexWidth() {
    return texWidth;
  }

  public int getWidth() {
    return width;
  }

  public ByteBuffer getImageBufferData() {
    ByteBuffer scratch = BufferUtils.createByteBuffer(rawData.length);
    scratch.put(rawData);
    scratch.flip();

    return scratch;
  }

  /**
   * Set a pixel in the image buffer
   * 
   * @param x The x position of the pixel to set
   * @param y The y position of the pixel to set
   * @param r The red component to set (0->255)
   * @param g The green component to set (0->255)
   * @param b The blue component to set (0->255)
   * @param a The alpha component to set (0->255)
   */
  public void setRGBA(int x, int y, int r, int g, int b, int a) {
    if ((x < 0) || (x >= width) || (y < 0) || (y >= height)) {
      throw new RuntimeException("Specified location: " + x + "," + y + " outside of image");
    }

    int ofs = ((x + (y * texWidth)) * 4);

    if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
      rawData[ofs] = (byte) b;
      rawData[ofs + 1] = (byte) g;
      rawData[ofs + 2] = (byte) r;
      rawData[ofs + 3] = (byte) a;
    } else {
      rawData[ofs] = (byte) r;
      rawData[ofs + 1] = (byte) g;
      rawData[ofs + 2] = (byte) b;
      rawData[ofs + 3] = (byte) a;
    }
  }

  public Image getImage() {
    return new Image(this);
  }

}
