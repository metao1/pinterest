package com.metao.asyncdownloader.repository.load.model.stream;

import android.support.annotation.Nullable;
import com.metao.asyncdownloader.repository.load.Option;
import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.data.HttpUrlFetcher;
import com.metao.asyncdownloader.repository.load.model.ImageDownloaderUrl;
import com.metao.asyncdownloader.repository.load.model.ModelCache;
import com.metao.asyncdownloader.repository.load.model.ModelLoader;
import com.metao.asyncdownloader.repository.load.model.ModelLoaderFactory;
import com.metao.asyncdownloader.repository.load.model.MultiModelLoaderFactory;
import java.io.InputStream;

/**
 * An {@link com.metao.asyncdownloader.repository.load.model.ModelLoader} for translating {@link
 * com.metao.asyncdownloader.repository.load.model.ImageDownloaderUrl} (http/https URLS) into {@link InputStream} data.
 */
public class HttpImageDownloaderUrlLoader implements ModelLoader<ImageDownloaderUrl, InputStream> {
  /**
   * An integer option that is used to determine the maximum connect and read timeout durations (in
   * milliseconds) for network connections.
   *
   * <p>Defaults to 2500ms.
   */
  public static final Option<Integer> TIMEOUT = Option.memory(
      "com.metao.asyncdownloader.repository.load.model.stream.HttpImageDownloaderUrlLoader.Timeout", 2500);

  @Nullable private final ModelCache<ImageDownloaderUrl, ImageDownloaderUrl> modelCache;

  public HttpImageDownloaderUrlLoader() {
    this(null);
  }

  public HttpImageDownloaderUrlLoader(ModelCache<ImageDownloaderUrl, ImageDownloaderUrl> modelCache) {
    this.modelCache = modelCache;
  }

  @Override
  public LoadData<InputStream> buildLoadData(ImageDownloaderUrl model, int width, int height,
      Options options) {
    // ImageDownloaderUrls memoize parsed URLs so caching them saves a few object instantiations and time
    // spent parsing urls.
    ImageDownloaderUrl url = model;
    if (modelCache != null) {
      url = modelCache.get(model, 0, 0);
      if (url == null) {
        modelCache.put(model, 0, 0, model);
        url = model;
      }
    }
    int timeout = options.get(TIMEOUT);
    return new LoadData<>(url, new HttpUrlFetcher(url, timeout));
  }

  @Override
  public boolean handles(ImageDownloaderUrl model) {
    return true;
  }

  /**
   * The default factory for {@link HttpImageDownloaderUrlLoader}s.
   */
  public static class Factory implements ModelLoaderFactory<ImageDownloaderUrl, InputStream> {
    private final ModelCache<ImageDownloaderUrl, ImageDownloaderUrl> modelCache = new ModelCache<>(500);

    @Override
    public ModelLoader<ImageDownloaderUrl, InputStream> build(MultiModelLoaderFactory multiFactory) {
      return new HttpImageDownloaderUrlLoader(modelCache);
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }
}
