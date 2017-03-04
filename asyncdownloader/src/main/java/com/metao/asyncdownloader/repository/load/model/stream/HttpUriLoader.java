package com.metao.asyncdownloader.repository.load.model.stream;

import android.net.Uri;
import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.model.ImageDownloaderUrl;
import com.metao.asyncdownloader.repository.load.model.ModelLoader;
import com.metao.asyncdownloader.repository.load.model.ModelLoaderFactory;
import com.metao.asyncdownloader.repository.load.model.MultiModelLoaderFactory;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Loads {@link InputStream}s from http or https {@link Uri}s.
 */
public class HttpUriLoader implements ModelLoader<Uri, InputStream> {
  private static final Set<String> SCHEMES =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList("http", "https")));

  private final ModelLoader<ImageDownloaderUrl, InputStream> urlLoader;

  public HttpUriLoader(ModelLoader<ImageDownloaderUrl, InputStream> urlLoader) {
    this.urlLoader = urlLoader;
  }

  @Override
  public LoadData<InputStream> buildLoadData(Uri model, int width, int height, Options options) {
    return urlLoader.buildLoadData(new ImageDownloaderUrl(model.toString()), width, height, options);
  }

  @Override
  public boolean handles(Uri model) {
    return SCHEMES.contains(model.getScheme());
  }

  /**
   * Factory for loading {@link InputStream}s from http/https {@link Uri}s.
   */
  public static class Factory implements ModelLoaderFactory<Uri, InputStream> {

    @Override
    public ModelLoader<Uri, InputStream> build(MultiModelLoaderFactory multiFactory) {
      return new HttpUriLoader(multiFactory.build(ImageDownloaderUrl.class, InputStream.class));
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }
}
