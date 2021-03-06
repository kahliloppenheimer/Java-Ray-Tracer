package me.kahlil.scene;

import static com.google.common.base.Preconditions.checkState;

import me.kahlil.graphics.MutableColor;
import me.kahlil.geometry.Vector;

/**
 * Represents the frame that the 3d scene is projected onto. This frame implementation is simply
 * parallel to the XY plane.
 */
public class Raster {
  private final Vector bottomLeftCorner;
  // Number of pixels for width in height of the frame
  private final int widthPx;
  private final int heightPx;
  private MutableColor[][] pixels;

  public Raster(int widthPx, int heightPx) {
    this.bottomLeftCorner = new Vector(-1, -1, -1);
    this.widthPx = widthPx;
    this.heightPx = heightPx;
    this.pixels = new MutableColor[heightPx][widthPx];
  }

  /** Returns the pixel at the specified coordinate */
  public MutableColor getPixel(int i, int j) {
    return pixels[i][j];
  }

  /** Sets the pixel at the specified coorindate */
  public void setPixel(int i, int j, MutableColor c) {
    checkState(pixels[i][j] == null, "Same pixel should not be modified twice: (%d, %d)", i, j);
    pixels[i][j] = c;
  }

  public int getWidthPx() {
    return widthPx;
  }

  public int getHeightPx() {
    return heightPx;
  }
}
