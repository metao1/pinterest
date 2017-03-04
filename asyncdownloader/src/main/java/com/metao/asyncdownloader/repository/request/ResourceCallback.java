package com.metao.asyncdownloader.repository.request;

import com.metao.asyncdownloader.repository.load.DataSource;
import com.metao.asyncdownloader.repository.load.engine.ImageDownloaderException;
import com.metao.asyncdownloader.repository.load.engine.Resource;

/**
 * A callback that listens for when a resource load completes successfully or fails due to an
 * exception.
 */
public interface ResourceCallback {

  /**
   * Called when a resource is successfully loaded.
   *
   * @param resource The loaded resource.
   */
  void onResourceReady(Resource<?> resource, DataSource dataSource);

  /**
   * Called when a resource fails to load successfully.
   *
   * @param e a non-null {@link ImageDownloaderException}.
   */
  void onLoadFailed(ImageDownloaderException e);
}
