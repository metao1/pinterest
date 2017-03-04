package com.metao.asyncdownloader.repository.core;

import android.util.Log;
import com.metao.asyncdownloader.repository.load.EncodeStrategy;
import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.ResourceEncoder;
import com.metao.asyncdownloader.repository.load.engine.Resource;
import com.metao.asyncdownloader.repository.util.ByteBufferUtil;
import java.io.File;
import java.io.IOException;

/**
 * Writes the original bytes of a {@link GifDrawable} to an
 * {@link java.io.OutputStream}.
 */
public class GifDrawableEncoder implements ResourceEncoder<GifDrawable> {
  private static final String TAG = "GifEncoder";

  @Override
  public EncodeStrategy getEncodeStrategy(Options options) {
    return EncodeStrategy.SOURCE;
  }

  @Override
  public boolean encode(Resource<GifDrawable> data, File file, Options options) {
    GifDrawable drawable = data.get();
    boolean success = false;
    try {
      ByteBufferUtil.toFile(drawable.getBuffer(), file);
      success = true;
    } catch (IOException e) {
      if (Log.isLoggable(TAG, Log.WARN)) {
        Log.w(TAG, "Failed to encode GIF drawable data", e);
      }
    }
    return success;
  }
}
