package com.metao.asyncdownloader.repository.load.model.stream;

import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.model.ImageDownloaderUrl;
import com.metao.asyncdownloader.repository.load.model.ModelLoader;
import com.metao.asyncdownloader.repository.load.model.ModelLoaderFactory;
import com.metao.asyncdownloader.repository.load.model.MultiModelLoaderFactory;
import java.io.InputStream;
import java.net.URL;

/**
 * A wrapper class that translates {@link URL} objects into {@link
 * com.metao.asyncdownloader.repository.load.model.ImageDownloaderUrl} objects and then uses the wrapped {@link
 * com.metao.asyncdownloader.repository.load.model.ModelLoader} for {@link com.metao.asyncdownloader.repository.load.model.ImageDownloaderUrl}s to
 * load the data.
 */
public class UrlLoader implements ModelLoader<URL, InputStream> {
  private final ModelLoader<ImageDownloaderUrl, InputStream> ImageDownloaderUrlLoader;

  public UrlLoader(ModelLoader<ImageDownloaderUrl, InputStream> ImageDownloaderUrlLoader) {
    this.ImageDownloaderUrlLoader = ImageDownloaderUrlLoader;
  }

  @Override
  public LoadData<InputStream> buildLoadData(URL model, int width, int height, Options options) {
    return ImageDownloaderUrlLoader.buildLoadData(new ImageDownloaderUrl(model), width, height, options);
  }

  @Override
  public boolean handles(URL model) {
    return true;
  }

  /**
   * Factory for loading {@link InputStream}s from {@link URL}s.
   */
  public static class StreamFactory implements ModelLoaderFactory<URL, InputStream> {

    @Override
    public ModelLoader<URL, InputStream> build(MultiModelLoaderFactory multiFactory) {
      return new UrlLoader(multiFactory.build(ImageDownloaderUrl.class, InputStream.class));
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }
}
