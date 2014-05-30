package ow.server.model;

import java.util.List;

import com.google.common.collect.Lists;

public class Planet extends Entity {

  public static final List<String> types = Lists.newArrayList("Mars", "Uranus", "Venus");

  public final String name;
  public final int color;
  public final List<Planet> connections = Lists.newCopyOnWriteArrayList();

  public Planet(String name, int color, double x, double y) {
    this.name = name;
    this.color = color;
    this.x = x;
    this.y = y;
  }

  public void connectTo(Planet p) {
    if (p == this || connections.contains(p)) {
      return;
    }

    connections.add(p);
    p.connections.add(this);
  }

}
