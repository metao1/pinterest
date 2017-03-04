package com.metao.asyncdownloader.repository.core;

import com.metao.asyncdownloader.repository.load.engine.Resource;
import com.metao.asyncdownloader.repository.load.resource.bytes.BytesResource;
import com.metao.asyncdownloader.repository.util.ByteBufferUtil;

import java.nio.ByteBuffer;

/**
 * An {@link ResourceTranscoder} that converts {@link
 * GifDrawable} into bytes by obtaining the original bytes of
 * the GIF from the {@link GifDrawable}.
 */
public class GifDrawableBytesTranscoder implements ResourceTranscoder<GifDrawable, byte[]> {
  @Override
  public Resource<byte[]> transcode(Resource<GifDrawable> toTranscode) {
    GifDrawable gifData = toTranscode.get();
    ByteBuffer byteBuffer = gifData.getBuffer();
    return new BytesResource(ByteBufferUtil.toBytes(byteBuffer));
  }
}
