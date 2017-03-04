package com.metao.asyncdownloader.repository.module;

import android.content.Context;
import com.metao.asyncdownloader.repository.core.ImageDownloaderBuilder;
import com.metao.asyncdownloader.repository.core.Registry;

/**
 * An interface allowing lazy configuration of ImageDownloader including setting options using
 * {@link com.metao.asyncdownloader.repository.core.ImageDownloaderBuilder} and registering
 * {@link com.metao.asyncdownloader.repository.load.model.ModelLoader ModelLoaders}.
 *
 * <p> To use this interface: <ol> <li> Implement the ImageDownloaderModule interface in a class with public
 * visibility, calling
 * {@link Registry#prepend(Class, Class, com.metao.asyncdownloader.repository.load.ResourceDecoder)} for each
 * {@link com.metao.asyncdownloader.repository.load.model.ModelLoader} you'd like to register:
 * <pre>
 *                  <code>
 *                      public class FlickrImageDownloaderModule implements ImageDownloaderModule {
 *                          {@literal @}Override
 *                          public void applyOptions(Context context, ImageDownloaderBuilder builder) {
 *                              builder.setDecodeFormat(DecodeFormat.ALWAYS_ARGB_8888);
 *                          }
 *
 *                          {@literal @}Override
 *                          public void registerComponents(Context context, ImageDownloader ImageDownloader) {
 *                              ImageDownloader.register(Model.class, Data.class, new MyModelLoader());
 *                          }
 *                      }
 *                  </code>
 *             </pre>
 * </li> <li> Add your implementation to your list of keeps in your proguard.cfg file:
 * <pre>
 *                  {@code
 *                      -keepnames class * com.metao.asyncdownloader.repository.imagehandler.samples.flickr.FlickrImageDownloaderModule
 *                  }
 *              </pre>
 * </li> <li> Add a metadata tag to your AndroidManifest.xml with your ImageDownloaderModule implementation's
 * fully qualified classname as the key, and {@code ImageDownloaderModule} as the value:
 * <pre>
 *                 {@code
 *                      <meta-data
 *                          android:name="com.metao.asyncdownloader.repository.imagehandler.samples.flickr.FlickrImageDownloaderModule"
 *                          android:value="ImageDownloaderModule" />
 *                 }
 *             </pre>
 * </li> </ol> </p>
 *
 * <p> All implementations must be publicly visible and contain only an empty constructor so they
 * can be instantiated via reflection when ImageDownloader is lazily initialized. </p>
 *
 * <p> There is no defined order in which modules are called, so projects should be careful to avoid
 * applying conflicting settings in different modules. If an application depends on libraries that
 * have conflicting modules, the application should consider avoiding the library modules and
 * instead providing their required dependencies in a single application module. </p>
 */
public interface ImageDownloaderModule {

  /**
   * Lazily apply options to a {@link com.metao.asyncdownloader.repository.core.ImageDownloaderBuilder} immediately before the ImageDownloader
   * singleton is created.
   *
   * <p> This method will be called once and only once per implementation. </p>
   *
   * @param context An Application {@link Context}.
   * @param builder The {@link com.metao.asyncdownloader.repository.core.ImageDownloaderBuilder} that will be used to create ImageDownloader.
   */
  void applyOptions(Context context, ImageDownloaderBuilder builder);

  /**
   * Lazily register components immediately after the ImageDownloader singleton is created but before any
   * requests can be started.
   *
   * <p> This method will be called once and only once per implementation. </p>
   *
   * @param context  An Application {@link Context}.
   * @param registry An {@link com.metao.asyncdownloader.repository.imagehandler.Registry} to use to register components.
   */
  void registerComponents(Context context, Registry registry);
}
