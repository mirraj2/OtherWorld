package ow.server.arch;

import org.testng.Assert;
import org.testng.annotations.Test;
import ow.server.arch.qtree.QuadTree;
import ow.server.arch.qtree.Query;

public class QuadTreeTest {

  @Test
  public void testGetNearest() {
    QuadTree<String> tree = new QuadTree<>(0, 0, 1000, 1000);

    tree.add("A", 20, 50);
    tree.add("B", 200, 200);
    tree.add("C", 250, 30);
    tree.add("D", 250, 30);
    tree.add("E", 10, 80);
    tree.add("F", 800, 250);
    tree.add("G", 5, 8);

    Assert.assertEquals(tree.singleSelect(Query.start(0, 0).limit(1)), "G");
    Assert.assertEquals(tree.singleSelect(Query.start(-1, -1).limit(1)), "G");
    Assert.assertEquals(tree.singleSelect(Query.start(11, 79).limit(1)), "E");

    // Assert.assertEquals(tree.getClosest(0, 0), "G");
  }

}
