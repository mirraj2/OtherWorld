package ow.server;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import ow.server.RTree.Locatable;

public class RTree<T extends Locatable> implements Iterable<T> {

  private final List<T> items = Lists.newArrayList();

  // private Page root = null;

  public void add(T item, double x, double y) {
    this.items.add(item);

    // if(root == null){
    // root = new Page
    // }
    // root.add(item, x, y);
  }

  public T find(double x, double y, double searchRadius) {
    searchRadius = searchRadius * searchRadius;
    
    for (T item : items) {
      double dx = item.getX() - x;
      double dy = item.getY() - y;
      if (dx * dx + dy * dy <= searchRadius) {
        return item;
      }
    }

    return null;
  }

  @Override
  public Iterator<T> iterator() {
    return Iterators.unmodifiableIterator(items.iterator());
  }

  // class Page {
  // private double x, y, width, height;
  // private T item;
  // private List<Page> children;
  //
  // public Page(T item, double x, double y) {
  // this.item = item;
  // this.x = x;
  // this.y = y;
  // this.width = 1;
  // this.height = 1;
  // }
  //
  // public void add(T item, double x, double y) {
  //
  // }
  // }

  public interface Locatable {
    public double getX();

    public double getY();
  }

}
