package com.metao.asyncdownloader.repository.load.engine;

import com.metao.asyncdownloader.repository.load.Encoder;
import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.engine.cache.DiskCache;
import java.io.File;

/**
 * Writes original source data or downsampled/transformed resource data to cache using the
 * provided {@link com.metao.asyncdownloader.repository.load.Encoder} or
 * {@link com.metao.asyncdownloader.repository.load.ResourceEncoder} and the given data or
 * {@link com.metao.asyncdownloader.repository.load.engine.Resource}.
 *
 * @param <DataType> The type of data that will be encoded (InputStream, ByteBuffer,
 *                  Resource<Bitmap> etc).
 */
class DataCacheWriter<DataType> implements DiskCache.Writer {
  private final Encoder<DataType> encoder;
  private final DataType data;
  private final Options options;

  DataCacheWriter(Encoder<DataType> encoder, DataType data, Options options) {
    this.encoder = encoder;
    this.data = data;
    this.options = options;
  }

  @Override
  public boolean write(File file) {
    return encoder.encode(data, file, options);
  }
}
