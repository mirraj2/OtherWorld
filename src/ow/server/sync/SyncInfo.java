package ow.server.sync;

import java.util.Set;

import ow.server.model.Ship;

import com.google.common.collect.Sets;

public class SyncInfo {

  public final Set<Ship> shipsSeen = Sets.newHashSet();
  public Set<Ship> shipsNearby = Sets.newHashSet();

}
