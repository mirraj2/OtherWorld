package ow.common;

import org.testng.annotations.Test;

public class MathTest {

  @Test
  public void atanTest() {
    for (int i = -1000; i <= 1000; i += 100) {
      for (int j = -1000; j <= 1000; j += 100) {
        double a = FastMath.atan2(i, j);
        double b = Math.atan2(i, j);
        System.out.println(a + " " + b);
      }
    }
  }

  public static void main(String[] args) {
    float min = -100;
    float max = +100;
    float step = 0.12f;

    for (int i = 0; i < 8; i++) {
      long t0A = System.nanoTime() / 1000000L;
      float sumA = 0.0f;
      for (float y = min; y < max; y += step)
        for (float x = min; x < max; x += step)
          sumA += FastMath.atan2(y, x);
      long t1A = System.nanoTime() / 1000000L;

      long t0B = System.nanoTime() / 1000000L;
      float sumB = 0.0f;
      for (float y = min; y < max; y += step)
        for (float x = min; x < max; x += step)
          sumB += Math.atan2(y, x);
      long t1B = System.nanoTime() / 1000000L;

      System.out.println();
      System.out.println("FastMath: " + (t1A - t0A) + "ms, sum=" + sumA);
      System.out.println("JavaMath: " + (t1B - t0B) + "ms, sum=" + sumB);
      System.out.println("factor: " + ((float) (t1B - t0B) / (t1A - t0A)));
    }
  }

}
