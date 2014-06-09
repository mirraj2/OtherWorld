package ow.server.arch.qtree;

import java.util.Iterator;
import java.util.List;

import ow.server.arch.qtree.QuadNode.QEntry;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;

public class QuadTree<T> implements Iterable<T> {

  private final QuadNode root;
  private List<T> items = Lists.newArrayList();
  private List<T> removed = Lists.newArrayList();

  public QuadTree(double x, double y, double w, double h) {
    this(new Rect(x, y, w, h));
  }

  public QuadTree(Rect bounds) {
    root = new QuadNode(bounds);
  }

  public void add(T item, double x, double y) {
    add(item, new Rect(x, y, 1, 1));
  }

  public void add(T item, Rect location) {
    checkArgument(root.r.contains(location));

    root.add(item, location);
    items.add(item);
  }

  public void remove(T item) {
    items.remove(item);
    removed.add(item);
  }

  public T singleSelect(Query query) {
    List<T> ret = select(query.limit(1));
    return getOnlyElement(ret, null);
  }

  @SuppressWarnings("unchecked")
  public List<T> select(Query query) {
    List<QEntry> results = Lists.newArrayList();
    root.select(query.end(), results);

    List<T> ret = Lists.newArrayList();
    for (QEntry e : results) {
      ret.add((T) e.item);
    }

    for (T item : removed) {
      ret.remove(item);
    }

    return ret;
  }

  public Rect getBounds() {
    return root.r;
  }

  // public T getClosest(double x, double y) {
  // return getClosest(x, y, Integer.MAX_VALUE);
  // }
  //
  // public T getClosest(double x, double y, double radius) {
  // List<T> nearby = getNearby(x, y, radius, 1);
  // return nearby.isEmpty() ? null : nearby.get(0);
  // }
  //
  // public List<T> getNearby(double x, double y, double radius) {
  // return getNearby(x, y, radius, Integer.MAX_VALUE);
  // }
  //
  // @SuppressWarnings("unchecked")
  // public List<T> getNearby(double x, double y, double radius, int limit) {
  // List<Object> ret = Lists.newArrayList();
  //
  // Rect r = new Rect(x - radius, y - radius, radius * 2, radius * 2);
  // double rSquared = radius * radius;
  //
  // checkState(!Double.isInfinite(rSquared));
  //
  // root.getNearby(x, y, rSquared, r, ret, limit);
  //
  // return (List<T>) ret;
  // }

  @Override
  public Iterator<T> iterator() {
    return items.iterator();
  }

}
