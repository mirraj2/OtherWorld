package ow.server.arch.qtree;

import java.util.Iterator;
import java.util.List;

import ow.server.arch.qtree.QuadNode.QEntry;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

  @Override
  public Iterator<T> iterator() {
    return items.iterator();
  }

  public JsonObject toJson() {
    JsonObject o = new JsonObject();
    o.addProperty("command", "quadtree");
    o.add("root", toJson(root));
    return o;
  }

  private JsonObject toJson(QuadNode node) {
    JsonObject ret = new JsonObject();
    ret.addProperty("x", node.r.x);
    ret.addProperty("y", node.r.y);
    ret.addProperty("w", node.r.w);
    ret.addProperty("h", node.r.h);

    if (!node.isLeaf()) {
      JsonArray a = new JsonArray();
      for (QuadNode child : node.getChildren()) {
        a.add(toJson(child));
      }
      ret.add("children", a);
    }

    return ret;
  }

}
