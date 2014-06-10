package ow.common;

import com.google.common.base.Stopwatch;

public class FastMath {

  private static final int ATAN2_BITS = 7;
  private static final int ATAN2_BITS2 = ATAN2_BITS * 2;
  private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
  private static final int ATAN2_COUNT = ATAN2_MASK + 1;
  private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

  private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);

  private static final float[] atan2 = new float[ATAN2_COUNT];

  static {
    Stopwatch watch = Stopwatch.createStarted();
    for (int i = 0; i < ATAN2_DIM; i++) {
      for (int j = 0; j < ATAN2_DIM; j++) {
        float x0 = (float) i / ATAN2_DIM;
        float y0 = (float) j / ATAN2_DIM;

        atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
      }
    }
    System.out.println("Created atan2 lookup table (" + ATAN2_COUNT + " entries): " + watch);
  }

  public static final float atan2(float y, float x) {
    float add, mul;

    if (x < 0.0f) {
      if (y < 0.0f) {
        x = -x;
        y = -y;

        mul = 1.0f;
      } else {
        x = -x;
        mul = -1.0f;
      }

      add = -3.141592653f;
    } else {
      if (y < 0.0f) {
        y = -y;
        mul = -1.0f;
      } else {
        mul = 1.0f;
      }

      add = 0.0f;
    }

    float invDiv = 1.0f / (((x < y) ? y : x) * INV_ATAN2_DIM_MINUS_1);

    int xi = (int) (x * invDiv);
    int yi = (int) (y * invDiv);

    return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
  }

}
