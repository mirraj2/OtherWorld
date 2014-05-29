package ow.client.model;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.newdawn.slick.Image;
import ow.client.ImageLoader;

public class Planet {

  public final int id;
  public final double x, y;
  public final String name;
  public final float r, g, b;
  public final List<Integer> connections = Lists.newArrayList();

  public Planet(int id, String name, int color, double x, double y, JsonArray connections) {
    this.id = id;
    this.name = name;

    Color c = new Color(color);
    r = c.getRed() / 255f;
    g = c.getGreen() / 255f;
    b = c.getBlue() / 255f;

    this.x = x;
    this.y = y;

    for (JsonElement e : connections) {
      this.connections.add(e.getAsInt());
    }
  }

  public Image getImage() {
    Image ret = ImageLoader.getSlickImage("planets/" + name + ".png");
    ret.setImageColor(r, g, b);
    return ret;
  }

}
