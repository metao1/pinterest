package com.metao.asyncpinteresthandler.database;

import com.metao.asyncpinteresthandler.database.elements.Chunk;
import com.metao.asyncpinteresthandler.database.elements.Task;
import com.metao.asyncpinteresthandler.repository.Repository;
import com.metao.asyncpinteresthandler.repository.RepositoryBuilder;
import com.metao.asyncpinteresthandler.repository.SizeOf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChunksDataSource {

    private Repository<Chunk> chunkRepository;

    public ChunksDataSource() {
        chunkRepository = new RepositoryBuilder<Chunk>("ChunksDataSource", 1)
                .useReferenceInRam(1024 * 1000, new AsyncHandlerSerializer())
                .build();
    }

    class AsyncHandlerSerializer implements SizeOf<Chunk> {

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

    public int insertChunks(Task task) {
        long lastChunkInserted = 0;
        if (task.size > 0) { // resumable
            long chunkSize = task.size / task.chunks;
            for (int i = 0; i < task.chunks; i++) {
                Chunk chunk = new Chunk(task.id);
                if (i == 0) {
                    chunk.begin = 0;
                } else {
                    chunk.begin = (chunkSize * i) + 1;
                }
                if (i == task.chunks - 1) {
                    chunk.end = task.size;
                } else {
                    chunk.end = chunkSize * (i + 1);
                }
                chunkRepository.put(chunk.taskId, chunk);
                lastChunkInserted = chunk.end;
            }
            return (int) lastChunkInserted - task.chunks + 1;
        } else {
            Chunk chunk = new Chunk(task.id);
            chunkRepository.put(chunk.taskId, chunk);
            return -1;
        }
    }

    public List<Chunk> chunksRelatedTask(String taskID) {
        List<Chunk> chunks = new ArrayList<>();
        ConcurrentHashMap<String, Chunk> chunkEntryIterator = chunkRepository.getRamCacheRepository();
        Chunk[] objects = chunkEntryIterator.values().toArray(new Chunk[]{});
        for (Chunk chunk : objects) {
            if (chunk.taskId.equalsIgnoreCase(taskID)) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    public boolean delete(int chunkID) {
        ConcurrentHashMap<String, Chunk> chunkEntryIterator = chunkRepository.getRamCacheRepository();
        Chunk[] objects = chunkEntryIterator.values().toArray(new Chunk[]{});
        for (Chunk chunk : objects) {
            if (chunk.id == chunkID) {
                chunkRepository.delete(chunk.taskId);
            }
        }
        return true;
    }
}
