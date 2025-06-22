package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskflow.adapter.TaskAdapter;
import com.example.taskflow.data.entity.Task;
import com.example.taskflow.data.entity.TaskPriority;
import com.example.taskflow.viewmodel.TaskViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;
    private FloatingActionButton fabAddTask;
    private TextView tvPendingCount, tvCompletedCount;
    private ChipGroup chipGroup;
    private Chip chipAll, chipPending, chipCompleted, chipHighPriority;

    // LiveData atual para observar
    private LiveData<List<Task>> currentTasksLiveData;

    private String currentFilter = "all";
    private boolean isUpdatingTask = false; // Flag para evitar conflitos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupObservers();
        setupClickListeners();
        setupFilterChips();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewTasks);
        fabAddTask = findViewById(R.id.fabAddTask);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        chipGroup = findViewById(R.id.chipGroup);

        chipAll = findViewById(R.id.chip_all);
        chipPending = findViewById(R.id.chip_pending);
        chipCompleted = findViewById(R.id.chip_completed);
        chipHighPriority = findViewById(R.id.chip_high_priority);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this);
        taskAdapter.setOnTaskActionListener(this);
        recyclerView.setAdapter(taskAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupViewModel() {
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
    }

    private void setupObservers() {
        // Observar contadores do dashboard - estes sempre ficam observando
        taskViewModel.getPendingTasksCount().observe(this, count -> {
            if (count != null) {
                tvPendingCount.setText(String.valueOf(count));
            }
        });

        taskViewModel.getCompletedTasksCount().observe(this, count -> {
            if (count != null) {
                tvCompletedCount.setText(String.valueOf(count));
            }
        });

        // Inicializar com todas as tarefas
        switchToFilter("all");
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivity(intent);
        });
    }

    private void setupFilterChips() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Evitar chamadas durante updates programáticos
            if (isUpdatingTask) return;

            if (checkedId == R.id.chip_all) {
                switchToFilter("all");
            } else if (checkedId == R.id.chip_pending) {
                switchToFilter("pending");
            } else if (checkedId == R.id.chip_completed) {
                switchToFilter("completed");
            } else if (checkedId == R.id.chip_high_priority) {
                switchToFilter("high_priority");
            }
        });
    }

    private void switchToFilter(String filter) {
        // Remove observador anterior se existir
        if (currentTasksLiveData != null) {
            currentTasksLiveData.removeObservers(this);
        }

        // Define novo LiveData baseado no filtro
        switch (filter) {
            case "all":
                currentTasksLiveData = taskViewModel.getAllTasks();
                break;
            case "pending":
                currentTasksLiveData = taskViewModel.getPendingTasks();
                break;
            case "completed":
                currentTasksLiveData = taskViewModel.getCompletedTasks();
                break;
            case "high_priority":
                currentTasksLiveData = taskViewModel.getHighPriorityTasks();
                break;
            default:
                currentTasksLiveData = taskViewModel.getAllTasks();
        }

        // Observa o novo LiveData
        currentTasksLiveData.observe(this, tasks -> {
            if (tasks != null) {
                taskAdapter.setTasks(tasks);
            }
        });

        currentFilter = filter;
    }

    @Override
    public void onTaskCompleteToggle(Task task) {
        if (isUpdatingTask) return; // Evita múltiplas chamadas simultâneas

        isUpdatingTask = true;

        // Cria uma cópia da tarefa para modificar
        Task updatedTask = createTaskCopy(task);
        boolean newCompletedState = !updatedTask.isCompleted();

        updatedTask.setCompleted(newCompletedState);

        if (newCompletedState) {
            // Marca como concluída com timestamp atual
            updatedTask.setCompletedAt(getCurrentBrazilianTime());
        } else {
            // Marca como pendente, remove timestamp de conclusão
            updatedTask.setCompletedAt(null);
        }

        // Atualiza no banco
        taskViewModel.update(updatedTask);

        // Feedback visual
        String message = newCompletedState ? "Tarefa concluída!" : "Tarefa reaberta!";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Reset flag após delay para evitar conflitos
        recyclerView.postDelayed(() -> isUpdatingTask = false, 100);
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Tarefa")
                .setMessage("Tem certeza que deseja excluir \"" + task.getTitle() + "\"?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    taskViewModel.delete(task);
                    Toast.makeText(this, "Tarefa excluída", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onTaskEdit(Task task) {
        // Método implementado para satisfazer a interface
        // A edição real é feita pelo TaskAdapter abrindo AddEditTaskActivity
    }

    // Método auxiliar para criar cópia da tarefa
    private Task createTaskCopy(Task original) {
        Task copy = new Task(original.getTitle(), original.getDescription(), original.getPriority());
        copy.setId(original.getId());
        copy.setCreatedAt(original.getCreatedAt());
        copy.setCompleted(original.isCompleted());
        copy.setCompletedAt(original.getCompletedAt());
        return copy;
    }

    // Método para obter hora atual no fuso horário brasileiro
    private Date getCurrentBrazilianTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        return calendar.getTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset flag quando retorna para a activity
        isUpdatingTask = false;
    }
}