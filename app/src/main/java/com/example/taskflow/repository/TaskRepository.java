package com.example.taskflow.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.taskflow.data.dao.TaskDao;
import com.example.taskflow.data.database.TaskDatabase;
import com.example.taskflow.data.entity.Task;
import com.example.taskflow.data.entity.TaskPriority;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;
    private LiveData<Integer> pendingTasksCount;    // Adicione
    private LiveData<Integer> completedTasksCount;  // Adicione
    private ExecutorService executorService;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getDatabase(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
        pendingTasksCount = taskDao.getPendingTasksCount();      // Adicione
        completedTasksCount = taskDao.getCompletedTasksCount();  // Adicione
        executorService = Executors.newSingleThreadExecutor();
    }
    // Método INSERT
    public void insert(Task task) {
        executorService.execute(() -> taskDao.insert(task));
    }

    // Método UPDATE - ADICIONE ESTE MÉTODO
    public void update(Task task) {
        executorService.execute(() -> taskDao.update(task));
    }

    // Método DELETE
    public void delete(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }

    // Getters
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return taskDao.getTasksByStatus(false);
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return taskDao.getTasksByStatus(true);
    }

    public LiveData<List<Task>> getHighPriorityTasks() {
        return taskDao.getTasksByPriority(TaskPriority.HIGH);
    }
    public LiveData<Integer> getPendingTasksCount() {
        return pendingTasksCount;
    }

    public LiveData<Integer> getCompletedTasksCount() {
        return completedTasksCount;
    }
}