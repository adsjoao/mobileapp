package com.example.taskflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.taskflow.data.entity.Task;
import com.example.taskflow.data.entity.TaskPriority;
import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY createdAt DESC")
    LiveData<List<Task>> getTasksByStatus(boolean isCompleted);

    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY createdAt DESC")
    LiveData<List<Task>> getTasksByPriority(TaskPriority priority);

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    LiveData<Integer> getPendingTasksCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    LiveData<Integer> getCompletedTasksCount();
}