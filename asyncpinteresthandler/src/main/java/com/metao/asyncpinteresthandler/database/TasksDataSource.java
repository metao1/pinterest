package com.metao.asyncpinteresthandler.database;

import com.metao.asyncpinteresthandler.core.enums.QueueSort;
import com.metao.asyncpinteresthandler.core.enums.TaskStates;
import com.metao.asyncpinteresthandler.database.elements.Task;
import com.metao.asyncpinteresthandler.repository.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.sort;

public class TasksDataSource {

    private Repository<Task> taskRepository;

    public TasksDataSource(Repository<Task> taskRepository) {
        this.taskRepository = taskRepository;
    }

    public String insertTask(Task task) {
        return taskRepository.put(task.id, task);
    }

    public boolean update(Task task) {
        Iterator taskEntryIterator = taskRepository.getRamCacheRepository().values().iterator();
        while (taskEntryIterator.hasNext()) {
            Task needToUpdateTask = (Task) taskEntryIterator.next();
            if (task.id.equalsIgnoreCase(needToUpdateTask.id)) {
                taskRepository.put(task.id, needToUpdateTask);
                return true;
            }
        }
        return false;
    }

    public List<Task> getTasksInState(int state) {
        List<Task> tasks = new ArrayList<>();
        ConcurrentHashMap ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator taskEntryIterator = ramCacheRepository.entrySet().iterator();
        while (taskEntryIterator.hasNext()) {
            Task task = (Task) taskEntryIterator.next();
            if (task.state == state) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    public List<Task> getUnNotifiedCompleted() {
        List<Task> completedTasks = new ArrayList<Task>();
        ConcurrentHashMap ramCacheRepository = taskRepository.getRamCacheRepository();
        for (Object object : ramCacheRepository.entrySet()) {
            Task task = (Task) object;
            if (!task.notify) {
                completedTasks.add(task);
            }
        }
        return completedTasks;
    }

    public List<Task> getUnCompletedTasks(int sortType) {
        List<Task> unCompleted = new ArrayList<>();
        ConcurrentHashMap<String, Task> ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator<Task> taskIterator = ramCacheRepository.values().iterator();
        while (taskIterator.hasNext()) {
            Task task = taskIterator.next();
            if (task.state != TaskStates.END) {
                unCompleted.add(task);
            }
        }
        Task[] tasks = unCompleted.toArray(new Task[]{});
        TaskComparator taskComparator = null;

        switch (sortType) {
            case QueueSort.HighPriority:
                taskComparator = new TaskComparator(1);
                break;
            case QueueSort.LowPriority:
                taskComparator = new TaskComparator(0);
                break;
            case QueueSort.oldestFirst:
                taskComparator = new TaskComparator(1);
                break;
            case QueueSort.earlierFirst:
                taskComparator = new TaskComparator(0);
                break;
            case QueueSort.HighToLowPriority:
                taskComparator = new TaskComparator(0);
                break;
            case QueueSort.LowToHighPriority:
                taskComparator = new TaskComparator(1);
                break;

        }
        sort(tasks, taskComparator);
        return Arrays.asList(tasks);
    }

    public Task getTaskInfo(String id) {
        ConcurrentHashMap ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator<Task> taskIterator = ramCacheRepository.values().iterator();
        while (taskIterator.hasNext()) {
            Task task = taskIterator.next();
            if (task.id.equalsIgnoreCase(id)) {
                return task;
            }
        }
        return null;
    }

    public Task getTaskInfoWithName(String name) {
        ConcurrentHashMap ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator<Task> taskIterator = ramCacheRepository.values().iterator();
        while (taskIterator.hasNext()) {
            Task task = taskIterator.next();
            if (task.name.equalsIgnoreCase(name)) {
                return task;
            }
        }
        return null;
    }

    public boolean delete(String taskID) {
        ConcurrentHashMap ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator<Task> taskIterator = ramCacheRepository.values().iterator();
        while (taskIterator.hasNext()) {
            Task task = taskIterator.next();
            if (task.id.equalsIgnoreCase(taskID)) {
                taskRepository.getRamCacheRepository().remove(task);
                return true;
            }
        }
        return false;
    }


    public boolean containsTask(String name) {
        boolean result = false;
        ConcurrentHashMap ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator<Task> taskIterator = ramCacheRepository.values().iterator();
        while (taskIterator.hasNext()) {
            Task task = taskIterator.next();
            if (task.name.equalsIgnoreCase(name)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean checkUnNotifiedTasks() {
        ConcurrentHashMap ramCacheRepository = taskRepository.getRamCacheRepository();
        for (Object object : ramCacheRepository.entrySet()) {
            Task task = (Task) object;
            if (!task.notify) {
                task.notify = true;
                ramCacheRepository.put(task.id, task);
            }
        }
        return true;
    }

    private class TaskComparator implements Comparator<Task> {
        int mode = -1;

        TaskComparator(int mode) {
            this.mode = mode;
        }

        @Override
        public int compare(Task task1, Task task2) {
            switch (mode) {
                case 1:
                    return (task1.priority && task2.priority) ? 1 : 0;
                case 2:
                    return (!task1.priority && !task2.priority) ? 1 : 0;
                default:
                    return -1;
            }
        }
    }
}
