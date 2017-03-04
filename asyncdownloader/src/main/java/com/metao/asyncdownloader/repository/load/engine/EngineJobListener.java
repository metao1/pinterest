package com.metao.asyncdownloader.repository.load.engine;

import com.metao.asyncdownloader.repository.load.Key;

interface EngineJobListener {

  void onEngineJobComplete(Key key, EngineResource<?> resource);

  void onEngineJobCancelled(EngineJob engineJob, Key key);
}
