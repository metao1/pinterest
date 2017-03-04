package com.metao.asyncdownloader.repository.core;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.metao.asyncdownloader.repository.load.DecodeFormat;
import com.metao.asyncdownloader.repository.load.engine.Engine;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.ArrayPool;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.BitmapPool;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.LruArrayPool;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.LruBitmapPool;
import com.metao.asyncdownloader.repository.load.engine.cache.DiskCache;
import com.metao.asyncdownloader.repository.load.engine.cache.InternalCacheDiskCacheFactory;
import com.metao.asyncdownloader.repository.load.engine.cache.LruResourceCache;
import com.metao.asyncdownloader.repository.load.engine.cache.MemoryCache;
import com.metao.asyncdownloader.repository.load.engine.cache.MemorySizeCalculator;
import com.metao.asyncdownloader.repository.load.engine.executor.ImageDownloaderExecutor;
import com.metao.asyncdownloader.repository.request.RequestOptions;

/**
 * A builder class for setting default structural classes for ImageDownloader to use.
 */
public final class ImageDownloaderBuilder {
  private final Context context;

  private Engine engine;
  private BitmapPool bitmapPool;
  private ArrayPool arrayPool;
  private MemoryCache memoryCache;
  private ImageDownloaderExecutor sourceExecutor;
  private ImageDownloaderExecutor diskCacheExecutor;
  private DiskCache.Factory diskCacheFactory;
  private MemorySizeCalculator memorySizeCalculator;
  private ConnectivityMonitorFactory connectivityMonitorFactory;
  private int logLevel = Log.INFO;
  private RequestOptions defaultRequestOptions = new RequestOptions();

  ImageDownloaderBuilder(Context context) {
    this.context = context.getApplicationContext();
  }

  /**
   * Sets the {@link com.metao.asyncdownloader.repository.imagehandler.load.engine.bitmap_recycle.BitmapPool} implementation to use
   * to store and retrieve reused {@link android.graphics.Bitmap}s.
   *
   * @param bitmapPool The pool to use.
   * @return This builder.
   */
  public ImageDownloaderBuilder setBitmapPool(BitmapPool bitmapPool) {
    this.bitmapPool = bitmapPool;
    return this;
  }

  /**
   * Sets the {@link ArrayPool} implementation to allow variable sized arrays to be stored
   * and retrieved as needed.
   *
   * @param arrayPool The pool to use.
   * @return This builder.
   */
  public ImageDownloaderBuilder setArrayPool(ArrayPool arrayPool) {
    this.arrayPool = arrayPool;
    return this;
  }

  /**
   * Sets the {@link com.metao.asyncdownloader.repository.imagehandler.load.engine.cache.MemoryCache} implementation to store
   * {@link com.metao.asyncdownloader.repository.imagehandler.load.engine.Resource}s that are not currently in use.
   *
   * @param memoryCache The cache to use.
   * @return This builder.
   */
  public ImageDownloaderBuilder setMemoryCache(MemoryCache memoryCache) {
    this.memoryCache = memoryCache;
    return this;
  }

  /**
   * Sets the {@link com.metao.asyncdownloader.repository.imagehandler.load.engine.cache.DiskCache} implementation to use to store
   * {@link com.metao.asyncdownloader.repository.imagehandler.load.engine.Resource} data and thumbnails.
   *
   * @param diskCache The disk cache to use.
   * @return This builder.
   * @deprecated Creating a disk cache directory on the main thread causes strict mode violations,
   * use {@link #setDiskCache(com.metao.asyncdownloader.repository.imagehandler.load.engine.cache.DiskCache.Factory)} instead.
   * Scheduled to be removed in ImageDownloader 4.0.
   */
  @Deprecated
  public ImageDownloaderBuilder setDiskCache(final DiskCache diskCache) {
    return setDiskCache(new DiskCache.Factory() {
      @Override
      public DiskCache build() {
        return diskCache;
      }
    });
  }

  /**
   * Sets the {@link com.metao.asyncdownloader.repository.imagehandler.load.engine.cache.DiskCache.Factory} implementation to use
   * to construct the {@link com.metao.asyncdownloader.repository.imagehandler.load.engine.cache.DiskCache} to use to store {@link
   * com.metao.asyncdownloader.repository.imagehandler.load.engine.Resource} data on disk.
   *
   * @param diskCacheFactory The disk cache factory to use.
   * @return This builder.
   */
  public ImageDownloaderBuilder setDiskCache(DiskCache.Factory diskCacheFactory) {
    this.diskCacheFactory = diskCacheFactory;
    return this;
  }

  /**
   * Sets the {@link java.util.concurrent.ExecutorService} implementation to use when retrieving
   * {@link com.metao.asyncdownloader.repository.imagehandler.load.engine.Resource}s that are not already in the cache.
   *
   * <p> Any implementation must order requests based on their {@link com.metao.asyncdownloader.repository.imagehandler.Priority}
   * for thumbnail requests to work properly.
   *
   * @param service The ExecutorService to use.
   * @return This builder.
   * @see #setDiskCacheExecutor(ImageDownloaderExecutor)
   * @see ImageDownloaderExecutor
   */
  public ImageDownloaderBuilder setResizeExecutor(ImageDownloaderExecutor service) {
    this.sourceExecutor = service;
    return this;
  }

  /**
   * Sets the {@link java.util.concurrent.ExecutorService} implementation to use when retrieving
   * {@link com.metao.asyncdownloader.repository.imagehandler.load.engine.Resource}s that are currently in cache.
   *
   * <p> Any implementation must order requests based on their {@link com.metao.asyncdownloader.repository.imagehandler.Priority}
   * for thumbnail requests to work properly.
   *
   * @param service The ExecutorService to use.
   * @return This builder.
   * @see #setResizeExecutor(ImageDownloaderExecutor)
   * @see ImageDownloaderExecutor
   */
  public ImageDownloaderBuilder setDiskCacheExecutor(ImageDownloaderExecutor service) {
    this.diskCacheExecutor = service;
    return this;
  }

  /**
   * Sets the default {@link RequestOptions} to use for all loads across the app.
   *
   * <p>Applying additional options with {@link
   * RequestBuilder#apply(com.metao.asyncdownloader.repository.imagehandler.request.BaseRequestOptions)} will override defaults
   * set here.
   *
   * @param requestOptions The options to use by default.
   * @return This builder.
   */
  public ImageDownloaderBuilder setDefaultRequestOptions(RequestOptions requestOptions) {
    this.defaultRequestOptions = requestOptions;
    return this;
  }

  /**
   * Sets the {@link com.metao.asyncdownloader.repository.imagehandler.load.DecodeFormat} that will be the default format for all
   * the default decoders that can change the {@link android.graphics.Bitmap.Config} of the {@link
   * android.graphics.Bitmap}s they decode.
   *
   * <p> Decode format is always a suggestion, not a requirement. See {@link
   * com.metao.asyncdownloader.repository.imagehandler.load.DecodeFormat} for more details. </p>
   *
   * @param decodeFormat The format to use.
   * @return This builder.
   *
   * @deprecated Use {@link #setDefaultRequestOptions(RequestOptions)} instead.
   */
  @Deprecated
  public ImageDownloaderBuilder setDecodeFormat(DecodeFormat decodeFormat) {
    defaultRequestOptions.apply(new RequestOptions().format(decodeFormat));
    return this;
  }

  /**
   * Sets the {@link MemorySizeCalculator} to use to calculate maximum sizes for default
   * {@link MemoryCache MemoryCaches} and/or default {@link BitmapPool BitmapPools}.
   *
   * @see #setMemorySizeCalculator(MemorySizeCalculator)
   *
   * @param builder The builder to use (will not be modified).
   * @return This builder.
   */
  public ImageDownloaderBuilder setMemorySizeCalculator(MemorySizeCalculator.Builder builder) {
    return setMemorySizeCalculator(builder.build());
  }

  /**
   * Sets the {@link MemorySizeCalculator} to use to calculate maximum sizes for default
   * {@link MemoryCache MemoryCaches} and/or default {@link BitmapPool BitmapPools}.
   *
   * <p>The given {@link MemorySizeCalculator} will not affect custom pools or caches provided
   * via {@link #setBitmapPool(BitmapPool)} or {@link #setMemoryCache(MemoryCache)}.
   *
   * @param calculator The calculator to use.
   * @return This builder.
   */
  public ImageDownloaderBuilder setMemorySizeCalculator(MemorySizeCalculator calculator) {
    this.memorySizeCalculator = calculator;
    return this;
  }

  /**
   * Sets the {@link com.metao.asyncdownloader.repository.imagehandler.manager.ConnectivityMonitorFactory}
   * to use to notify {@link com.metao.asyncdownloader.repository.imagehandler.RequestManager} of connectivity events.
   * If not set {@link com.metao.asyncdownloader.repository.imagehandler.manager.DefaultConnectivityMonitorFactory} would be used.
   *
   * @param factory The factory to use
   * @return This builder.
   */
  public ImageDownloaderBuilder setConnectivityMonitorFactory(ConnectivityMonitorFactory factory) {
    this.connectivityMonitorFactory = factory;
    return this;
  }

  /**
   * Sets a log level constant from those in {@link Log} to indicate the desired log verbosity.
   *
   * <p>The level must be one of {@link Log#VERBOSE}, {@link Log#DEBUG}, {@link Log#INFO},
   * {@link Log#WARN}, or {@link Log#ERROR}.
   *
   * <p>{@link Log#VERBOSE} means one or more lines will be logged per request, including
   * timing logs and failures. {@link Log#DEBUG} means at most one line will be logged
   * per successful request, including timing logs, although many lines may be logged for
   * failures including multiple complete stack traces. {@link Log#INFO} means
   * failed loads will be logged including multiple complete stack traces, but successful loads
   * will not be logged at all. {@link Log#WARN} means only summaries of failed loads will be
   * logged. {@link Log#ERROR} means only exceptional cases will be logged.
   *
   * <p>All logs will be logged using the 'ImageDownloader' tag.
   *
   * <p>Many other debugging logs are available in individual classes. The log level supplied here
   * only controls a small set of informative and well formatted logs. Users wishing to debug
   * certain aspects of the library can look for individual <code>TAG</code> variables at the tops
   * of classes and use <code>adb shell setprop log.tag.TAG</code> to enable or disable any relevant
   * tags.
   *
   * @param logLevel The log level to use from {@link Log}.
   * @return This builder.
   */
  public ImageDownloaderBuilder setLogLevel(int logLevel) {
    if (logLevel < Log.VERBOSE || logLevel > Log.ERROR) {
      throw new IllegalArgumentException("Log level must be one of Log.VERBOSE, Log.DEBUG,"
          + " Log.INFO, Log.WARN, or Log.ERROR");
    }
    this.logLevel = logLevel;
    return this;
  }

  // For testing.
  ImageDownloaderBuilder setEngine(Engine engine) {
    this.engine = engine;
    return this;
  }

  ImageDownloader createImageDownloader() {
    if (sourceExecutor == null) {
      sourceExecutor = ImageDownloaderExecutor.newSourceExecutor();
    }

    if (diskCacheExecutor == null) {
      diskCacheExecutor = ImageDownloaderExecutor.newDiskCacheExecutor();
    }

    if (memorySizeCalculator == null) {
      memorySizeCalculator = new MemorySizeCalculator.Builder(context).build();
    }

    if (connectivityMonitorFactory == null) {
      connectivityMonitorFactory = new DefaultConnectivityMonitorFactory();
    }

    if (bitmapPool == null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        int size = memorySizeCalculator.getBitmapPoolSize();
        bitmapPool = new LruBitmapPool(size);
      } else {
        bitmapPool = new BitmapPoolAdapter();
      }
    }

    if (arrayPool == null) {
      arrayPool = new LruArrayPool(memorySizeCalculator.getArrayPoolSizeInBytes());
    }

    if (memoryCache == null) {
      memoryCache = new LruResourceCache(memorySizeCalculator.getMemoryCacheSize());
    }

    if (diskCacheFactory == null) {
      diskCacheFactory = new InternalCacheDiskCacheFactory(context);
    }

    if (engine == null) {
      engine = new Engine(memoryCache, diskCacheFactory, diskCacheExecutor, sourceExecutor,
          ImageDownloaderExecutor.newUnlimitedSourceExecutor());
    }

    return new ImageDownloader(
        context,
        engine,
        memoryCache,
        bitmapPool,
        arrayPool,
        connectivityMonitorFactory,
        logLevel,
        defaultRequestOptions.lock());
  }
}
