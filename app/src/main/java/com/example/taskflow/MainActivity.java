package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu; // IMPORT ADICIONADO
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
        // Observar todas as tarefas
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

    // IMPLEMENTAÇÃO CORRETA DA INTERFACE TaskAdapter.OnTaskActionListener

    @Override
    public void onTaskCompleteToggle(Task task) {
        // Usa o método do ViewModel conforme documentação
        taskViewModel.toggleTaskCompletion(task);
        Toast.makeText(this,
                task.isCompleted() ? "Tarefa marcada como concluída" : "Tarefa reaberta",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Tarefa")
                .setMessage("Tem certeza que deseja excluir esta tarefa?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    taskViewModel.deleteTask(task); // Usa método do ViewModel
                    Toast.makeText(this, "Tarefa excluída", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onTaskEdit(Task task) {
        // Implementação completa do método de edição
        // Abre a AddEditTaskActivity em modo de edição
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK_PRIORITY, task.getPriority().name());
        startActivity(intent);
    }

    // MÉTODOS AUXILIARES REMOVIDOS/CORRIGIDOS

    // Método showTaskMenu corrigido - agora recebe a view correta
    private void showTaskMenu(android.view.View view, Task task) {
        PopupMenu popup = new PopupMenu(this, view); // View correta passada como parâmetro
        popup.getMenuInflater().inflate(R.menu.task_menu, popup.getMenu());

        // Ajusta texto do menu baseado no status da tarefa
        popup.getMenu().findItem(R.id.action_toggle_status)
                .setTitle(task.isCompleted() ? "Marcar como pendente" : "Marcar como concluída");

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                onTaskEdit(task);
                return true;
            } else if (itemId == R.id.action_delete) {
                onTaskDelete(task);
                return true;
            } else if (itemId == R.id.action_toggle_status) {
                onTaskCompleteToggle(task);
                return true;
            }
            return false;
        });
        popup.show();
    }

    // MÉTODOS DESNECESSÁRIOS REMOVIDOS
    // Os métodos onTaskClick, onTaskLongClick, onCheckboxClick, onMenuClick
    // foram removidos pois não são necessários com a implementação atual
}