package com.metao.async;

import android.util.Log;
import com.metao.async.download.appConstants.Helper;
import com.metao.async.download.database.elements.Chunk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Created by metao on 2/1/2017.
 */
public class Repository<T> {

    public enum RepositoryType {
        JSON(1),
        XML(2),
        NORMAL(3),
        BITMAP(4),
        STRING(5);
        private int type = 0;

        RepositoryType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    private static final int RAM_SIZE = 10 * 1024;
    private final int hashCode;
    private DownloadHandler<T> downloadHandler;
    private final CacheSerializer<T> ramSerializer;
    private final SizeOf<Chunk> sizeOf;
    private Type type;
    private RepositoryCache ramCacheRepository;
    private RepositoryType repositoryType = RepositoryType.STRING;
    private RepositoryCacheRamMode ramMode;
    private String repoName;

    public Repository(String repoName) {
        this.type = getSuperclassTypeParameter(getClass());
        this.hashCode = type.hashCode();
        this.repoName = repoName;
        ramCacheRepository = new RepositoryCache(RAM_SIZE);
        ramMode = RepositoryCacheRamMode.ENABLE_WITH_REFERENCE;
        ramSerializer = new RamSerializer();
        sizeOf = new ChunkSize();

    }

    public Repository<T> addDownload(final String url
            , RepositoryCallback<T> repositoryCallback) {
        if (contains(url)) {//check cache for data availability
            T t = get(url);
            repositoryCallback.onDownloadFinished(url, t);
            Log.d("tag", "using cache");
        } else {
            switch (ramMode) {
                case ENABLE_WITH_SPECIFIC_SERIALIZER:
                    this.ramCacheRepository = new StringRepositoryCache(RAM_SIZE);
                    break;
                case ENABLE_WITH_REFERENCE:
                    this.ramCacheRepository = new ReferenceRepositoryCache<>(RAM_SIZE, sizeOf);
                    break;
                default:
                    this.ramCacheRepository = null;
            }
            String taskId = UUID.randomUUID().toString();
            MessageArg messageArg = new MessageArg(taskId);
            messageArg.setJobRepositoryType(repositoryType());
            messageArg.setType(getType());
            messageArg.setUrl(url);
            downloadHandler = new DownloadHandler<>();
            downloadHandler.setRepoCallback(repositoryCallback);
            downloadHandler.setCacheRepository();
            downloadHandler.setTaskRepository();
            downloadHandler.setChunkRepository();
            downloadHandler.execute(messageArg);
        }
        return this;
    }

    class ChunkSize implements SizeOf<Chunk> {

        @Override
        public int sizeOf(Chunk object) {
            int size = 0;
            size += 4; // we suppose that char = 4 bytes
            size += object.taskId.length() * 4; // we suppose that char = 4 bytes
            size += 1; // we suppose that char = 4 bytes
            size += 4; // we suppose that char = 4 bytes
            size += object.taskId.length() * 4; // we suppose that char = 4 bytes
            size += 1; // we suppose that char = 4 bytes
            return size;
        }
    }

    public int ramSize() {
        return RAM_SIZE;
    }

    public RepositoryType repositoryType() {
        return repositoryType;
    }

    private static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }

    public final Type getType() {
        return type;
    }

    public long getRamUsedInBytes() {
        return ramCacheRepository.snapshot().size();
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    public String put(String key, T object) {
        if (!ramCacheRepository.contains(key)) {
            if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_REFERENCE)) {
                assert ramCacheRepository != null;
                Log.d("cache", key + " store into cache");
                ramCacheRepository.put(key, object);
            }
            String ramSerialized = null;
            if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                ramSerialized = ramSerializer.toString(object);
                ramCacheRepository.put(key, ramSerialized);
            }
        }
        return Helper.createNewId();
    }

    public T get(String key) {
        Object ramResult = null;
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

    public boolean delete(String key) {
        if (!ramMode.equals(RepositoryCacheRamMode.DISABLE)) {
            ramCacheRepository.remove(key);
            return true;
        }
        return false;
    }

    public void invalidate() {
        invalidateRAM();
    }

    public void invalidateRAM() {
        if (!ramMode.equals(RepositoryCacheRamMode.DISABLE)) {
            ramCacheRepository.evictAll();
        }
    }

    public boolean contains(String key) {
        if (!ramMode.equals(RepositoryCacheRamMode.DISABLE) && ramCacheRepository.contains(key)) {
            return true;
        }
        return false;
    }

    public RepositoryCache getRamCacheRepository() {
        return ramCacheRepository;
    }

    private class RamSerializer implements CacheSerializer<T> {
        @Override
        public T fromString(String data) {
            return null;
        }

        @Override
        public String toString(T object) {
            return null;
        }
    }
}