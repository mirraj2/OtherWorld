package ow.server.arch.qtree;

import org.testng.internal.Nullable;

public class Query {

  public final double x, y;

  @Nullable
  public Integer limit;

  @Nullable
  public Double radius;

  @Nullable
  public Rect bounds;

  private Query(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public Query limit(int limit) {
    this.limit = limit;
    return this;
  }

  public Query radius(double radius) {
    this.radius = radius;

    return this;
  }

  /**
   * Marks that the query is finished being written.
   */
  public Query end() {
    if (radius != null) {
      bounds = new Rect(x - radius, y - radius, radius * 2, radius * 2);
    }
    if (limit == null) {
      limit = Integer.MAX_VALUE;
    }
    return this;
  }

  public static Query start(double x, double y) {
    return new Query(x, y);
  }

}
