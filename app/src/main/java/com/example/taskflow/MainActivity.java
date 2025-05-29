package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;
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
        // Observar todas as tarefas por padrão
        taskViewModel.getAllTasks().observe(this, tasks -> {
            if (tasks != null) {
                taskAdapter.setTasks(tasks);
            }
        });

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
                taskViewModel.getAllTasks().observe(this, taskAdapter::setTasks);
            } else if (checkedId == R.id.chip_pending) {
                currentFilter = "pending";
                taskViewModel.getPendingTasks().observe(this, taskAdapter::setTasks);
            } else if (checkedId == R.id.chip_completed) {
                currentFilter = "completed";
                taskViewModel.getCompletedTasks().observe(this, taskAdapter::setTasks);
            } else if (checkedId == R.id.chip_high_priority) {
                currentFilter = "high_priority";
                taskViewModel.getHighPriorityTasks().observe(this, taskAdapter::setTasks);
            }
        });
    }

    // Implementação da interface TaskAdapter.OnTaskActionListener

    @Override
    public void onTaskCompleteToggle(Task task) {
        taskViewModel.toggleTaskCompletion(task);
        Toast.makeText(this,
                task.isCompleted() ? "Tarefa marcada como concluída" : "Tarefa marcada como pendente",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Tarefa")
                .setMessage("Tem certeza que deseja excluir a tarefa \"" + task.getTitle() + "\"?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    taskViewModel.delete(task);
                    Toast.makeText(this, "Tarefa excluída com sucesso", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onTaskEdit(Task task) {
        // Este método é chamado pelo adapter quando necessário
        // A implementação real da edição está no TaskAdapter
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_PRIORITY, task.getPriority().name());
        startActivity(intent);
    }

    // Métodos auxiliares para compatibilidade (caso sejam chamados de outros lugares)
    public void onTaskClick(Task task) {
        Toast.makeText(this, "Tarefa: " + task.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public void onTaskLongClick(Task task) {
        onTaskEdit(task); // Clique longo abre para edição
    }

    public void onCheckboxClick(Task task) {
        onTaskCompleteToggle(task);
    }

    public void onMenuClick(Task task) {
        showTaskMenu(task);
    }

    private void showTaskMenu(Task task) {
        // Nota: Este método precisa de uma view para ancorar o popup
        // O TaskAdapter já implementa o menu, então este método é opcional
        Toast.makeText(this, "Menu da tarefa: " + task.getTitle(), Toast.LENGTH_SHORT).show();
    }
}