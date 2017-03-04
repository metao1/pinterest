package com.metao.asyncdownloader.repository.load.resource;

import com.metao.asyncdownloader.repository.load.engine.Resource;
import com.metao.asyncdownloader.repository.util.Preconditions;

/**
 * Simple wrapper for an arbitrary object which helps to satisfy some of the ImageDownloader engine's
 * contracts. <b>Suggested usages only include resource object which don't have size and cannot be
 * recycled/closed.</b>
 *
 * @param <T> type of the wrapped resource
 */
// TODO: there isn't much point in caching these...
public class SimpleResource<T> implements Resource<T> {
  protected final T data;

  public SimpleResource(T data) {
    this.data = Preconditions.checkNotNull(data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<T> getResourceClass() {
    return (Class<T>) data.getClass();
  }

  @Override
  public final T get() {
    return data;
  }

  @Override
  public final int getSize() {
    return 1;
  }

  @Override
  public void recycle() {
    // no op
  }
}
