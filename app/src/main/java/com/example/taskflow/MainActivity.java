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

    // Observer atual para gerenciar corretamente
    private Observer<List<Task>> currentTaskObserver;
    private LiveData<List<Task>> currentLiveData;

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
        // Observar contadores do dashboard
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

        // Configurar observador inicial para "todas as tarefas"
        setupTaskObserver(taskViewModel.getAllTasks());
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivity(intent);
        });
    }

    private void setupFilterChips() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            LiveData<List<Task>> newLiveData = null;

            if (checkedId == R.id.chip_all) {
                currentFilter = "all";
                newLiveData = taskViewModel.getAllTasks();
            } else if (checkedId == R.id.chip_pending) {
                currentFilter = "pending";
                newLiveData = taskViewModel.getPendingTasks();
            } else if (checkedId == R.id.chip_completed) {
                currentFilter = "completed";
                newLiveData = taskViewModel.getCompletedTasks();
            } else if (checkedId == R.id.chip_high_priority) {
                currentFilter = "high_priority";
                newLiveData = taskViewModel.getHighPriorityTasks();
            }

            if (newLiveData != null) {
                setupTaskObserver(newLiveData);
            }
        });
    }

    /**
     * Método para gerenciar corretamente os observadores de tarefas
     */
    private void setupTaskObserver(LiveData<List<Task>> newLiveData) {
        // Remove o observador anterior se existir
        if (currentLiveData != null && currentTaskObserver != null) {
            currentLiveData.removeObserver(currentTaskObserver);
        }

        // Cria um novo observador
        currentTaskObserver = tasks -> {
            if (tasks != null) {
                // Limpar a lista antes de definir as novas tarefas
                taskAdapter.setTasks(null);
                // Definir as novas tarefas
                taskAdapter.setTasks(tasks);
            }
        };

        // Configura o novo observador
        currentLiveData = newLiveData;
        currentLiveData.observe(this, currentTaskObserver);
    }

    // Implementação da interface TaskAdapter.OnTaskActionListener

    @Override
    public void onTaskCompleteToggle(Task task) {
        // Criar uma nova instância da task para evitar problemas de referência
        Task updatedTask = new Task(task.getTitle(), task.getDescription(), task.getPriority());
        updatedTask.setId(task.getId());
        updatedTask.setCreatedAt(task.getCreatedAt());
        updatedTask.setCompleted(!task.isCompleted());

        if (updatedTask.isCompleted()) {
            updatedTask.setCompletedAt(new Date());
        } else {
            updatedTask.setCompletedAt(null);
        }

        taskViewModel.update(updatedTask);

        // Mostra feedback para o usuário
        String message = updatedTask.isCompleted() ? "Tarefa concluída!" : "Tarefa reaberta!";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Tarefa")
                .setMessage("Tem certeza que deseja excluir esta tarefa?\n\n" + task.getTitle())
                .setPositiveButton("Sim", (dialog, which) -> {
                    taskViewModel.delete(task);
                    Toast.makeText(this, "Tarefa excluída!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onTaskEdit(Task task) {
        // Método implementado para satisfazer a interface
        // A edição real é feita pelo TaskAdapter abrindo AddEditTaskActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpa o observador atual para evitar vazamentos de memória
        if (currentLiveData != null && currentTaskObserver != null) {
            currentLiveData.removeObserver(currentTaskObserver);
        }
    }
}