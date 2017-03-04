package com.metao.asyncdownloader.repository.core;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import com.metao.asyncdownloader.repository.load.engine.Resource;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.BitmapPool;
import com.metao.asyncdownloader.repository.util.Preconditions;

/**
 * An {@link ResourceTranscoder} that converts {@link
 * Bitmap}s into {@link BitmapDrawable}s.
 */
public class BitmapDrawableTranscoder implements ResourceTranscoder<Bitmap, BitmapDrawable> {
  private final Resources resources;
  private final BitmapPool bitmapPool;

  public BitmapDrawableTranscoder(Context context) {
    this(context.getResources(), ImageDownloader.get(context).getBitmapPool());
  }

  public BitmapDrawableTranscoder(Resources resources, BitmapPool bitmapPool) {
    this.resources = Preconditions.checkNotNull(resources);
    this.bitmapPool = Preconditions.checkNotNull(bitmapPool);
  }

  @Override
  public Resource<BitmapDrawable> transcode(Resource<Bitmap> toTranscode) {
    return LazyBitmapDrawableResource.obtain(resources, bitmapPool, toTranscode.get());
  }
}
