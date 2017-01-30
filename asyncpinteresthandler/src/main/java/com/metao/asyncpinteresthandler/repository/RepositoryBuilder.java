package com.metao.asyncpinteresthandler.repository;

/**
 * Class used to build a cache.
 *
 * @param <T> is the class of object to store in cache.
 */
public class RepositoryBuilder<T> {

    private String id;
    private int appVersion;
    private int maxRamSizeBytes;
    private RepositoryCacheRamMode ramMode;
    private CacheSerializer<T> ramSerializer;
    private SizeOf<T> sizeOf;

    /**
     * Start the building of the cache.
     *
     * @param id         is the id of the cache (should be unique).
     * @param appVersion is the app version of the app. If data are already stored in disk cache
     *                   with previous app version, it will be invalidate.
     */
    public RepositoryBuilder(String id, int appVersion) {
        this.id = id;
        this.appVersion = appVersion;
        this.ramMode = null;
    }

    /**
     * RepositoryBuilder the cache. Exception will be thrown if it can not be created.
     *
     * @return the cache instance.
     */
    public Repository<T> build() {
        if (ramMode == null) {
            throw new IllegalStateException("No ram mode set");
        }
        Repository<T> cache = new Repository<>(
                ramMode,
                ramSerializer,
                maxRamSizeBytes,
                sizeOf
        );
        return cache;
    }

    /**
     * Use Json serialization/deserialization to store and retrieve object from ram cache.
     *
     * @param maxRamSizeBytes is the max amount of ram in bytes which can be used by the ram cache.
     * @param serializer      is the cache interface which provide serialization/deserialization
     *                        methods
     *                        for the ram cache layer.
     * @return the builder.
     */
    public RepositoryBuilder<T> useSerializerInRam(
            int maxRamSizeBytes, CacheSerializer<T> serializer) {
        this.ramMode = RepositoryCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER;
        this.maxRamSizeBytes = maxRamSizeBytes;
        this.ramSerializer = serializer;
        return this;
    }

    /**
     * Store directly objects in ram (without serialization/deserialization).
     * You have to provide a way to compute the size of an object in
     * ram to be able to used the Repository capacity of the ram cache.
     *
     * @param maxRamSizeBytes is the max amount of ram which can be used by the ram cache.
     * @param handlerSizeOf   computes the size of object stored in ram.
     * @return the builder.
     */
    public RepositoryBuilder<T> useReferenceInRam(
            int maxRamSizeBytes, SizeOf<T> handlerSizeOf
    ) {
        this.ramMode = RepositoryCacheRamMode.ENABLE_WITH_REFERENCE;
        this.maxRamSizeBytes = maxRamSizeBytes;
        this.sizeOf = handlerSizeOf;
        return this;
    }

}
