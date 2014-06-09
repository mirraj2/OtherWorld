package ow.server.arch.qtree;

import java.util.List;

import ow.common.OMath;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

class QuadNode {
  private static final int NODE_CAPACITY = 4;

  public final Rect r;

  private List<QEntry> items;
  private QuadNode[] children;

  public QuadNode(double x, double y, double w, double h) {
    this(new Rect(x, y, w, h));
  }

  public QuadNode(Rect r) {
    this.r = r;
  }

  public void add(Object item, Rect location) {
    add(new QEntry(item, location));
  }

  private void add(QEntry entry) {
    if (!r.intersects(entry.location)) {
      return;
    }

    if (!isLeaf()) {
      addToChildren(entry);
      return;
    }

    if (items == null) {
      items = Lists.newArrayListWithCapacity(NODE_CAPACITY + 1);
    }
    items.add(entry);
    if (items.size() > NODE_CAPACITY) {
      split();
    }
  }

  public void select(Query query, List<QEntry> results) {
    if (query.bounds == null) {
      selectUnbounded(query, results);
      if (results.size() != 1) {
        selectUnbounded(query, results);
      }
      checkState(results.size() == 1);
    } else {
      double rSquared = query.radius * query.radius;
      checkState(!Double.isInfinite(rSquared));
      checkState(!Double.isNaN(rSquared));
      getNearby(query.x, query.y, rSquared, query.bounds, results, query.limit);
    }
  }

  private void selectUnbounded(Query query, List<QEntry> results) {
    checkArgument(query.limit == 1, "Unbounded queries are currently only supported with a limit of one.");

    Rect searchRegion;

    QuadNode node = getLeafNode(query.x, query.y);
    if (node == null) {
      searchRegion = r;
    } else {
      searchRegion = node.r.centerOn(query.x, query.y);
    }

    while (true) {
      getNearby(query.x, query.y, Double.MAX_VALUE, searchRegion, results, Integer.MAX_VALUE);
      
      if (!results.isEmpty()) {
        trimClosest(results, query);
        return;
      }

      if (searchRegion.contains(r)) {
        return;
      }

      // double the size of the search region
      searchRegion = searchRegion.doubleSize();
    }
  }

  private void trimClosest(List<QEntry> results, Query query) {
    QEntry closest = null;
    double minD = -1;

    for (QEntry result : results) {
      double d = OMath.distanceSquared(query.x, result.location.centerX(), query.y, result.location.centerY());
      if (closest == null || d < minD) {
        minD = d;
        closest = result;
      }
    }

    results.clear();
    results.add(closest);
  }

  private QuadNode getLeafNode(double x, double y) {
    if (isLeaf()) {
      if (r.contains(x, y)) {
        return this;
      }
    } else {
      for (QuadNode child : children) {
        QuadNode q = child.getLeafNode(x, y);
        if (q != null) {
          return q;
        }
      }
    }
    return null;
  }

  private void getNearby(double x, double y, double radiusSqrd, Rect location, List<QEntry> buffer, int limit) {
    if (buffer.size() == limit) {
      return;
    }

    if (!r.intersects(location)) {
      return;
    }

    if (!isLeaf()) {
      for (QuadNode child : children) {
        child.getNearby(x, y, radiusSqrd, location, buffer, limit);
      }
      return;
    }

    if (items != null) {
      for (QEntry e : items) {
        double d = OMath.distanceSquared(e.location.centerX(), x, e.location.centerY(), y);
        if (d <= radiusSqrd) {
          buffer.add(e);
          if (buffer.size() == limit) {
            break;
          }
        }
      }
    }
  }

  private void split() {
    if (r.w <= 64) {
      return;
    }

    // System.out.println("Splitting " + r);

    double mx = r.w / 2;
    double my = r.h / 2;
    children = new QuadNode[NODE_CAPACITY];
    children[0] = new QuadNode(r.x, r.y, mx, my);
    children[1] = new QuadNode(r.x + mx, r.y, mx, my);
    children[2] = new QuadNode(r.x, r.y + my, mx, my);
    children[3] = new QuadNode(r.x + mx, r.y + my, mx, my);

    for (QEntry item : items) {
      addToChildren(item);
    }

    items.clear();
    items = null;
  }

  private void addToChildren(QEntry entry) {
    for (QuadNode child : children) {
      child.add(entry);
    }
  }

  private boolean isLeaf() {
    return children == null;
  }

  static final class QEntry {
    public final Object item;
    public final Rect location;

    private QEntry(Object item, Rect location) {
      this.item = item;
      this.location = location;
    }

    @Override
    public String toString() {
      return item.toString();
    }
  }
}
