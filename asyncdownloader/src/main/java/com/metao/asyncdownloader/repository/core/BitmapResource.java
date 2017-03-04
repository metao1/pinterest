package com.metao.asyncdownloader.repository.core;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import com.metao.asyncdownloader.repository.load.engine.Initializable;
import com.metao.asyncdownloader.repository.load.engine.Resource;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.BitmapPool;
import com.metao.asyncdownloader.repository.util.Preconditions;
import com.metao.asyncdownloader.repository.util.Util;

/**
 * A resource wrapping a {@link Bitmap} object.
 */
public class BitmapResource implements Resource<Bitmap>,
    Initializable {
  private final Bitmap bitmap;
  private final BitmapPool bitmapPool;

  /**
   * Returns a new {@link BitmapResource} wrapping the given {@link Bitmap} if the Bitmap is
   * non-null or null if the given Bitmap is null.
   *
   * @param bitmap     A Bitmap.
   * @param bitmapPool A non-null {@link com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.BitmapPool}.
   */
  @Nullable
  public static BitmapResource obtain(@Nullable Bitmap bitmap, BitmapPool bitmapPool) {
    if (bitmap == null) {
      return null;
    } else {
      return new BitmapResource(bitmap, bitmapPool);
    }
  }

  public BitmapResource(Bitmap bitmap, BitmapPool bitmapPool) {
    this.bitmap = Preconditions.checkNotNull(bitmap, "Bitmap must not be null");
    this.bitmapPool = Preconditions.checkNotNull(bitmapPool, "BitmapPool must not be null");
  }

  @Override
  public Class<Bitmap> getResourceClass() {
    return Bitmap.class;
  }

  @Override
  public Bitmap get() {
    return bitmap;
  }

  @Override
  public int getSize() {
    return Util.getBitmapByteSize(bitmap);
  }

  @Override
  public void recycle() {
    bitmapPool.put(bitmap);
  }

  @Override
  public void initialize() {
    bitmap.prepareToDraw();
  }
}
