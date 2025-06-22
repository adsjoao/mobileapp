package com.example.taskflow.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String description;
    private TaskPriority priority;
    private boolean isCompleted;
    private Date createdAt;
    private Date completedAt;

    // Construtor
    public Task(String title, String description, TaskPriority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.isCompleted = false;
        this.createdAt = getCurrentBrazilianTime();
        this.completedAt = null;
    }

    // Método para obter hora atual no fuso horário brasileiro
    private Date getCurrentBrazilianTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        return calendar.getTime();
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        // NÃO define automaticamente completedAt aqui para dar mais controle
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    // Método para toggle status com controle completo
    public void toggleCompletedStatus() {
        this.isCompleted = !this.isCompleted;
        if (this.isCompleted) {
            this.completedAt = getCurrentBrazilianTime();
        } else {
            this.completedAt = null;
        }
    }

    // Método para marcar como concluída
    public void markAsCompleted() {
        this.isCompleted = true;
        this.completedAt = getCurrentBrazilianTime();
    }

    // Método para marcar como pendente
    public void markAsPending() {
        this.isCompleted = false;
        this.completedAt = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isCompleted=" + isCompleted +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                '}';
    }

    // Método para criar uma cópia da tarefa
    public Task copy() {
        Task copy = new Task(this.title, this.description, this.priority);
        copy.setId(this.id);
        copy.setCreatedAt(this.createdAt);
        copy.setCompleted(this.isCompleted);
        copy.setCompletedAt(this.completedAt);
        return copy;
    }
}