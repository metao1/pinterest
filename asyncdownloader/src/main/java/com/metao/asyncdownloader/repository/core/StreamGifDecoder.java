package com.metao.asyncdownloader.repository.core;

import android.util.Log;
import com.metao.asyncdownloader.repository.load.ImageHeaderParser;
import com.metao.asyncdownloader.repository.load.ImageHeaderParser.ImageType;
import com.metao.asyncdownloader.repository.load.ImageHeaderParserUtils;
import com.metao.asyncdownloader.repository.load.Option;
import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.ResourceDecoder;
import com.metao.asyncdownloader.repository.load.engine.Resource;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.ArrayPool;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * A relatively inefficient decoder for {@link GifDrawable}
 * that converts {@link InputStream}s to {@link ByteBuffer}s and then passes
 * the buffer to a wrapped decoder.
 */
public class StreamGifDecoder implements ResourceDecoder<InputStream, GifDrawable> {
  private static final String TAG = "StreamGifDecoder";
  /**
   * If set to {@code true}, disables this decoder
   * ({@link #handles(InputStream, Options)} will return {@code false}). Defaults to
   * {@code false}.
   */
  public static final Option<Boolean> DISABLE_ANIMATION = Option.memory(
      "com.metao.asyncdownloader.repository.core.ByteBufferGifDecoder.DisableAnimation", false);

  private final List<ImageHeaderParser> parsers;
  private final ResourceDecoder<ByteBuffer, GifDrawable> byteBufferDecoder;
  private final ArrayPool byteArrayPool;

  public StreamGifDecoder(List<ImageHeaderParser> parsers, ResourceDecoder<ByteBuffer,
      GifDrawable> byteBufferDecoder, ArrayPool byteArrayPool) {
    this.parsers = parsers;
    this.byteBufferDecoder = byteBufferDecoder;
    this.byteArrayPool = byteArrayPool;
  }

  @Override
  public boolean handles(InputStream source, Options options) throws IOException {
    return !options.get(DISABLE_ANIMATION)
        && ImageHeaderParserUtils.getType(parsers, source, byteArrayPool) == ImageType.GIF;
  }

  @Override
  public Resource<GifDrawable> decode(InputStream source, int width, int height,
      Options options) throws IOException {
    byte[] data = inputStreamToBytes(source);
    if (data == null) {
      return null;
    }
    ByteBuffer byteBuffer = ByteBuffer.wrap(data);
    return byteBufferDecoder.decode(byteBuffer, width, height, options);
  }

  private static byte[] inputStreamToBytes(InputStream is) {
    final int bufferSize = 16384;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferSize);
    try {
      int nRead;
      byte[] data = new byte[bufferSize];
      while ((nRead = is.read(data)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
    } catch (IOException e) {
      if (Log.isLoggable(TAG, Log.WARN)) {
        Log.w(TAG, "Error reading data from stream", e);
      }
      return null;
    }
    return buffer.toByteArray();
  }
}
