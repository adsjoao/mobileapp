package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskflow.adapter.TaskAdapter;
import com.example.taskflow.data.entity.Task;
import com.example.taskflow.viewmodel.TaskViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;
    private FloatingActionButton fabAddTask;
    private TextView tvPendingCount, tvCompletedCount;
    private ChipGroup chipGroup;
    private Chip chipAll, chipPending, chipCompleted, chipHighPriority;

    private String currentFilter = "all";
    private LiveData<List<Task>> currentTasksLiveData;
    private Observer<List<Task>> currentTasksObserver;

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
        // Observar contadores do dashboard (estes nunca mudam)
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

        // Configurar observer inicial para "Todas"
        applyFilter("all");
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivity(intent);
        });
    }

    private void setupFilterChips() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Remove observer anterior para evitar conflitos
            removeCurrentObserver();

            // Aplica o novo filtro
            if (checkedId == R.id.chip_all) {
                applyFilter("all");
            } else if (checkedId == R.id.chip_pending) {
                applyFilter("pending");
            } else if (checkedId == R.id.chip_completed) {
                applyFilter("completed");
            } else if (checkedId == R.id.chip_high_priority) {
                applyFilter("high_priority");
            }
        });
    }

    private void removeCurrentObserver() {
        if (currentTasksLiveData != null && currentTasksObserver != null) {
            currentTasksLiveData.removeObserver(currentTasksObserver);
        }
    }

    private void applyFilter(String filter) {
        currentFilter = filter;

        // Remove observer anterior
        removeCurrentObserver();

        // Cria novo observer
        currentTasksObserver = tasks -> {
            if (tasks != null) {
                taskAdapter.setTasks(tasks);
            }
        };

        // Aplica o filtro correto
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
        currentTasksLiveData.observe(this, currentTasksObserver);
    }

    // Implementação da interface TaskAdapter.OnTaskActionListener
    @Override
    public void onTaskCompleteToggle(Task task) {
        // Alterna o status
        task.setCompleted(!task.isCompleted());

        // Define ou remove a data de conclusão
        if (task.isCompleted()) {
            task.setCompletedAt(new Date());
        } else {
            task.setCompletedAt(null);
        }

        // Atualiza no banco
        taskViewModel.update(task);

        // Feedback para o usuário
        String message = task.isCompleted() ? "Tarefa concluída" : "Tarefa reaberta";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // IMPORTANTE: O filtro atual já será automaticamente atualizado
        // porque estamos observando LiveData que reage às mudanças no banco
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Tarefa")
                .setMessage("Tem certeza que deseja excluir \"" + task.getTitle() + "\"?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    taskViewModel.delete(task);
                    Toast.makeText(this, "Tarefa excluída", Toast.LENGTH_SHORT).show();
                    // A lista será automaticamente atualizada pelo LiveData
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onTaskEdit(Task task) {
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_PRIORITY, task.getPriority().name());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpa observers para evitar memory leaks
        removeCurrentObserver();
    }
}