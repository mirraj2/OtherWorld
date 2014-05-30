package ow.server.ai;

import java.util.concurrent.TimeUnit;

public class Task {

  final long duration;

  long lastTime = 0;

  public Task(int n, TimeUnit unit){
    duration = unit.toMillis(n);
  }

  public boolean isReady() {
    long now = System.currentTimeMillis();
    if (now - lastTime >= duration) {
      lastTime = now;
      return true;
    }
    return false;
  }

  public void reset() {
    lastTime = 0;
  }

  public static Task every(int n, TimeUnit unit) {
    return new Task(n, unit);
  }

}
