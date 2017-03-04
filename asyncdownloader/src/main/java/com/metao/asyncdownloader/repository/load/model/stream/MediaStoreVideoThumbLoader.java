package com.metao.asyncdownloader.repository.load.model.stream;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.data.mediastore.MediaStoreUtil;
import com.metao.asyncdownloader.repository.core.ThumbFetcher;
import com.metao.asyncdownloader.repository.load.model.ModelLoader;
import com.metao.asyncdownloader.repository.load.model.ModelLoaderFactory;
import com.metao.asyncdownloader.repository.load.model.MultiModelLoaderFactory;
import com.metao.asyncdownloader.repository.core.VideoBitmapDecoder;
import com.metao.asyncdownloader.repository.signature.ObjectKey;
import java.io.InputStream;

/**
 * Loads {@link InputStream}s from media store video {@link Uri}s that point to pre-generated
 * thumbnails for those {@link Uri}s in the media store.
 *
 * <p>If {@link VideoBitmapDecoder#TARGET_FRAME} is set with a non-null value that is not equal to
 * {@link VideoBitmapDecoder#DEFAULT_FRAME}, this loader will always return {@code null}. The media
 * store does not use a defined frame to generate the thumbnail, so we cannot accurately fulfill
 * requests for specific frames.
 */
public class MediaStoreVideoThumbLoader implements ModelLoader<Uri, InputStream> {
  private final Context context;

  MediaStoreVideoThumbLoader(Context context) {
    this.context = context.getApplicationContext();
  }

  @Override
  @Nullable
  public LoadData<InputStream> buildLoadData(Uri model, int width, int height, Options options) {
    if (MediaStoreUtil.isThumbnailSize(width, height) && isRequestingDefaultFrame(options)) {
      // TODO(nnaze): Tighten down this call to just the dependencies neede by buildVideoFetcher
      return new LoadData<>(new ObjectKey(model), ThumbFetcher.buildVideoFetcher(context, model));
    } else {
      return null;
    }
  }

  private boolean isRequestingDefaultFrame(Options options) {
    Long specifiedFrame = options.get(VideoBitmapDecoder.TARGET_FRAME);
    return specifiedFrame != null && specifiedFrame == VideoBitmapDecoder.DEFAULT_FRAME;
  }

  @Override
  public boolean handles(Uri model) {
    return MediaStoreUtil.isMediaStoreVideoUri(model);
  }

  /**
   * Loads {@link InputStream}s from media store image {@link Uri}s that point to pre-generated
   * thumbnails for those {@link Uri}s in the media store.
   */
  public static class Factory implements ModelLoaderFactory<Uri, InputStream> {

    private final Context context;

    public Factory(Context context) {
      this.context = context;
    }

    @Override
    public ModelLoader<Uri, InputStream> build(MultiModelLoaderFactory multiFactory) {
      return new MediaStoreVideoThumbLoader(context);
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }
}
