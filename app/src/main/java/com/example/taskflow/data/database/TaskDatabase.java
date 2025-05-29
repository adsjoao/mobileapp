package com.example.taskflow.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.taskflow.data.converter.Converters;
import com.example.taskflow.data.dao.TaskDao;
import com.example.taskflow.data.entity.Task;

@Database(
        entities = {Task.class},
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class TaskDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();

    private static volatile TaskDatabase INSTANCE;

    public static TaskDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (TaskDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TaskDatabase.class,
                            "task_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}