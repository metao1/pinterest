package com.metao.pinterest.views.adapters;

import com.metao.asyncpinteresthandler.database.elements.Task;
import com.metao.asyncpinteresthandler.repository.SizeOf;

public class AsyncHandlerSerializer implements SizeOf<Task> {

    @Override
    public int sizeOf(Task object) {
        int size = 0;
        size += object.id.length() * 4; // we suppose that char = 4 bytes
        size += object.repositoryId.length() * 4; // we suppose that char = 4 bytes
        size += 4; // we suppose that char = 4 bytes
        size += 1; // we suppose that char = 4 bytes
        size += object.extension.length() * 4; // we suppose that char = 4 bytes
        size += object.name.length() * 4; // we suppose that char = 4 bytes
        size += object.url.length() * 4; // we suppose that char = 4 bytes
        size += object.chunks * 4; // we suppose that char = 4 bytes
        return size;
    }
}
