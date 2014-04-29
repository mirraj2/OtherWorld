package ow.client;


public class Ship {

  private double x, y;
  private double rotation;
  private String image;

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getRotation() {
    return rotation;
  }

  public String getImage() {
    return image;
  }

  public void setLocation(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void setImage(String image) {
    this.image = image;
  }

}
