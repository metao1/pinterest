package com.metao.asyncdownloader.repository.load.engine;

import com.metao.asyncdownloader.repository.load.Key;
import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.Transformation;
import java.util.Map;

class EngineKeyFactory {

  @SuppressWarnings("rawtypes")
  public EngineKey buildKey(Object model, Key signature, int width, int height,
      Map<Class<?>, Transformation<?>> transformations, Class<?> resourceClass,
      Class<?> transcodeClass, Options options) {
    return new EngineKey(model, signature, width, height, transformations, resourceClass,
        transcodeClass, options);
  }
}
