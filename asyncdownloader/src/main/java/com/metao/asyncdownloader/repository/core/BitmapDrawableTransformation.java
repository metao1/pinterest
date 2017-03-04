package com.metao.asyncdownloader.repository.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import com.metao.asyncdownloader.repository.load.Transformation;
import com.metao.asyncdownloader.repository.load.engine.Resource;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.BitmapPool;
import com.metao.asyncdownloader.repository.util.Preconditions;
import java.security.MessageDigest;

/**
 * Transforms {@link BitmapDrawable}s.
 */
public class BitmapDrawableTransformation implements Transformation<BitmapDrawable> {

  private final Context context;
  private final BitmapPool bitmapPool;
  private final Transformation<Bitmap> wrapped;

  public BitmapDrawableTransformation(Context context, Transformation<Bitmap> wrapped) {
    this(context, ImageDownloader.get(context).getBitmapPool(), wrapped);
  }

  // Visible for testing.
  BitmapDrawableTransformation(Context context, BitmapPool bitmapPool,
      Transformation<Bitmap> wrapped) {
    this.context = context.getApplicationContext();
    this.bitmapPool = Preconditions.checkNotNull(bitmapPool);
    this.wrapped = Preconditions.checkNotNull(wrapped);
  }

  @Override
  public Resource<BitmapDrawable> transform(Resource<BitmapDrawable> drawableResourceToTransform,
      int outWidth, int outHeight) {
    BitmapDrawable drawableToTransform = drawableResourceToTransform.get();
    Bitmap bitmapToTransform = drawableToTransform.getBitmap();

    BitmapResource bitmapResourceToTransform = BitmapResource.obtain(bitmapToTransform, bitmapPool);
    Resource<Bitmap> transformedBitmapResource =
        wrapped.transform(bitmapResourceToTransform, outWidth, outHeight);

    if (transformedBitmapResource.equals(bitmapResourceToTransform)) {
      return drawableResourceToTransform;
    } else {
      return LazyBitmapDrawableResource.obtain(context, transformedBitmapResource.get());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof BitmapDrawableTransformation) {
      BitmapDrawableTransformation other = (BitmapDrawableTransformation) o;
      return wrapped.equals(other.wrapped);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return wrapped.hashCode();
  }

  @Override
  public void updateDiskCacheKey(MessageDigest messageDigest) {
    wrapped.updateDiskCacheKey(messageDigest);
  }
}
