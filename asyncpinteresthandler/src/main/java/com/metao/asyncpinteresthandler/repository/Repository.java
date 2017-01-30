/*
 * Copyright 2014 Vincent Brison.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metao.asyncpinteresthandler.repository;

import com.metao.asyncpinteresthandler.appConstants.Helper;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class intent to provide a very easy to use, reliable, highly configurable caching library
 * for Android.
 *
 * @param <T> is the Class of object to cache.
 */
public class Repository<T> {

    private final RepositoryCache ramCacheRepository;
    private final RepositoryCacheRamMode ramMode;
    private final CacheSerializer<T> ramSerializer;

    public Repository(RepositoryCacheRamMode ramMode,
                      CacheSerializer<T> ramSerializer,
                      int maxRamSizeBytes,
                      SizeOf<T> sizeOf) {
        this.ramMode = ramMode;
        this.ramSerializer = ramSerializer;
        switch (ramMode) {
            case ENABLE_WITH_SPECIFIC_SERIALIZER:
                this.ramCacheRepository = new StringRepositoryCache(maxRamSizeBytes);
                break;
            case ENABLE_WITH_REFERENCE:
                this.ramCacheRepository = new ReferenceRepositoryCache<>(maxRamSizeBytes, sizeOf);
                break;
            default:
                this.ramCacheRepository = null;
        }
    }

    public long getRamUsedInBytes() {
        if (ramCacheRepository == null) {
            return -1;
        } else {
            return ramCacheRepository.size();
        }
    }

    public ConcurrentHashMap getRamCacheRepository() {
        return this.ramCacheRepository.getConcurrentHashMap();
    }

    /**
     * Return the way objects are cached in RAM layer.
     *
     * @return the way objects are cached in RAM layer.
     */
    public RepositoryCacheRamMode getRAMMode() {
        return ramMode;
    }

    /**
     * Put an object in cache.
     *
     * @param key    is the key of the object.
     * @param object is the object to put in cache.
     */
    public String put(String key, T object) {
        // Synchronize put on each entry. Gives concurrent editions on different entries, and atomic
        // modification on the same entry.
        if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_REFERENCE)) {
            assert ramCacheRepository != null;
            ramCacheRepository.put(key, object);
        }

        String ramSerialized = null;
        if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            ramSerialized = ramSerializer.toString(object);
            ramCacheRepository.put(key, ramSerialized);
        }
        return Helper.createNewId();
    }

    /**
     * Return the object of the corresponding key from the cache. In no object is available,
     * return null.
     *
     * @param key is the key of the object.
     * @return the object of the corresponding key from the cache. In no object is available,
     * return null.
     */
    public T get(String key) {
        Object ramResult = null;
        // Try to get the object from RAM.
        boolean isRamSerialized = ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER);
        boolean isRamReferenced = ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_REFERENCE);
        if (isRamSerialized || isRamReferenced) {
            ramResult = ramCacheRepository.get(key);
        }

        if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_REFERENCE)) {
            return (T) ramResult;
        } else if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            return ramSerializer.fromString((String) ramResult);
        }
        // No data is available.
        return null;
    }

    /**
     * Delete the corresponding object in cache.
     *
     * @param key is the key of the object.
     */
    public void delete(String key) {
        if (!ramMode.equals(RepositoryCacheRamMode.DISABLE)) {
            ramCacheRepository.remove(key);
        }
    }

    /**
     * Remove all objects from cache (both RAM and disk).
     */
    public void invalidate() {
        invalidateRAM();
    }

    /**
     * Remove all objects from RAM.
     */
    public void invalidateRAM() {
        if (!ramMode.equals(RepositoryCacheRamMode.DISABLE)) {
            ramCacheRepository.evictAll();
        }
    }

    /**
     * Test if an object is present in cache.
     *
     * @param key is the key of the object.
     * @return true if the object is present in cache, false otherwise.
     */
    public boolean contains(String key) {
        if (!ramMode.equals(RepositoryCacheRamMode.DISABLE) && ramCacheRepository.snapshot().containsKey(key)) {
            return true;
        }
        return false;
    }
}
