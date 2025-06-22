package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

        // CORREÇÃO: Observar todas as tarefas por padrão
        applyCurrentFilter();
    }

    private void setupClickListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivity(intent);
        });
    }

    private void setupFilterChips() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentFilter = "all";
            } else if (checkedId == R.id.chip_pending) {
                currentFilter = "pending";
            } else if (checkedId == R.id.chip_completed) {
                currentFilter = "completed";
            } else if (checkedId == R.id.chip_high_priority) {
                currentFilter = "high_priority";
            }
            applyCurrentFilter();
        });
    }

    // CORREÇÃO: Método centralizado para aplicar filtros
    private void applyCurrentFilter() {
        switch (currentFilter) {
            case "all":
                taskViewModel.getAllTasks().observe(this, tasks -> {
                    if (tasks != null) {
                        taskAdapter.setTasks(tasks);
                    }
                });
                break;
            case "pending":
                taskViewModel.getPendingTasks().observe(this, tasks -> {
                    if (tasks != null) {
                        taskAdapter.setTasks(tasks);
                    }
                });
                break;
            case "completed":
                taskViewModel.getCompletedTasks().observe(this, tasks -> {
                    if (tasks != null) {
                        taskAdapter.setTasks(tasks);
                    }
                });
                break;
            case "high_priority":
                taskViewModel.getHighPriorityTasks().observe(this, tasks -> {
                    if (tasks != null) {
                        taskAdapter.setTasks(tasks);
                    }
                });
                break;
        }
    }

    // CORREÇÃO: Implementação correta do toggle de status
    @Override
    public void onTaskCompleteToggle(Task task) {
        // Inverte o status atual
        boolean newStatus = !task.isCompleted();
        task.setCompleted(newStatus);

        // Define a data de conclusão
        if (newStatus) {
            task.setCompletedAt(new Date());
            Toast.makeText(this, "Tarefa marcada como concluída", Toast.LENGTH_SHORT).show();
        } else {
            task.setCompletedAt(null);
            Toast.makeText(this, "Tarefa marcada como pendente", Toast.LENGTH_SHORT).show();
        }

        // Atualiza no banco de dados
        taskViewModel.update(task);

        // CORREÇÃO: Reaplica o filtro atual para atualizar a lista
        applyCurrentFilter();
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Tarefa")
                .setMessage("Tem certeza que deseja excluir esta tarefa?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    taskViewModel.delete(task);
                    Toast.makeText(this, "Tarefa excluída", Toast.LENGTH_SHORT).show();
                    // Reaplica o filtro atual
                    applyCurrentFilter();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onTaskEdit(Task task) {
        // A edição é tratada pelo TaskAdapter abrindo a AddEditTaskActivity
        // Este método está aqui apenas para satisfazer a interface
    }

    @Override
    protected void onResume() {
        super.onResume();
        // CORREÇÃO: Recarrega os dados quando volta para a tela
        applyCurrentFilter();
    }
}