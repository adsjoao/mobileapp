package com.example.taskflow.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.data.entity.Task;
import com.example.taskflow.data.entity.TaskPriority;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private LiveData<List<Task>> allTasks;
    private LiveData<Integer> pendingTasksCount;    // Adicione esta linha
    private LiveData<Integer> completedTasksCount;  // Adicione esta linha

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
        pendingTasksCount = repository.getPendingTasksCount();      // Adicione esta linha
        completedTasksCount = repository.getCompletedTasksCount();  // Adicione esta linha
    }

    // Métodos CRUD existentes...
    public void insert(Task task) {
        repository.insert(task);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }

    // Getters para LiveData
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return repository.getPendingTasks();
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return repository.getCompletedTasks();
    }

    public LiveData<List<Task>> getHighPriorityTasks() {
        return repository.getHighPriorityTasks();
    }

    // ADICIONE ESTES MÉTODOS
    public LiveData<Integer> getPendingTasksCount() {
        return pendingTasksCount;
    }

    public LiveData<Integer> getCompletedTasksCount() {
        return completedTasksCount;
    }
}