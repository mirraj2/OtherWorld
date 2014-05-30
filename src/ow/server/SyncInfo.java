package ow.server;

import java.util.Set;

import com.google.common.collect.Sets;

public class SyncInfo {

  public final Set<Ship> shipsSeen = Sets.newHashSet();
  public Set<Ship> shipsNearby = Sets.newHashSet();

}
