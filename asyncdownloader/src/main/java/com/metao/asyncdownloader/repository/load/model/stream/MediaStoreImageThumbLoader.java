package com.metao.asyncdownloader.repository.load.model.stream;

import android.content.Context;
import android.net.Uri;
import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.data.mediastore.MediaStoreUtil;
import com.metao.asyncdownloader.repository.core.ThumbFetcher;
import com.metao.asyncdownloader.repository.load.model.ModelLoader;
import com.metao.asyncdownloader.repository.load.model.ModelLoaderFactory;
import com.metao.asyncdownloader.repository.load.model.MultiModelLoaderFactory;
import com.metao.asyncdownloader.repository.signature.ObjectKey;
import java.io.InputStream;

/**
 * Loads {@link InputStream}s from media store image {@link Uri}s that point to pre-generated
 * thumbnails for those {@link Uri}s in the media store.
 */
public class MediaStoreImageThumbLoader implements ModelLoader<Uri, InputStream> {
  public final Context context;

  public MediaStoreImageThumbLoader(Context context) {
    this.context = context.getApplicationContext();
  }

  @Override
  public LoadData<InputStream> buildLoadData(Uri model, int width, int height, Options options) {
    if (MediaStoreUtil.isThumbnailSize(width, height)) {
      return new LoadData<>(new ObjectKey(model), ThumbFetcher.buildImageFetcher(context, model));
    } else {
      return null;
    }
  }

  @Override
  public boolean handles(Uri model) {
    return MediaStoreUtil.isMediaStoreImageUri(model);
  }

  /**
   * Factory that loads {@link InputStream}s from media store image {@link Uri}s.
   */
  public static class Factory implements ModelLoaderFactory<Uri, InputStream> {

    private final Context context;

    public Factory(Context context) {
      this.context = context;
    }

    @Override
    public ModelLoader<Uri, InputStream> build(MultiModelLoaderFactory multiFactory) {
      return new MediaStoreImageThumbLoader(context);
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }
}
