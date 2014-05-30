package ow.server.arch;

import java.util.Set;

import com.google.common.collect.Sets;

public class SwapSet<T> {

  private Set<T> current = Sets.newHashSet();
  private Set<T> backup = Sets.newHashSet();

  public boolean isEmpty() {
    return current.isEmpty();
  }

  public Set<T> get() {
    backup.clear();

    Set<T> temp = current;
    current = backup;
    backup = temp;

    return backup;
  }

  public void add(T item) {
    current.add(item);
  }

  public static <T> SwapSet<T> create() {
    return new SwapSet<T>();
  }

}
