package com.example.taskflow;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.taskflow.data.database.TaskDatabase;
import com.example.taskflow.data.entity.Task;           // ← ADICIONE ESTE IMPORT
import com.example.taskflow.data.entity.TaskPriority;  // ← ADICIONE ESTE IMPORT
import com.example.taskflow.viewmodel.TaskViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_TASK_DESCRIPTION = "task_description";
    public static final String EXTRA_TASK_PRIORITY = "task_priority";

    private TextInputEditText etTitle, etDescription;
    private TextInputLayout tilTitle, tilDescription;
    private RadioGroup rgPriority;
    private RadioButton rbHigh, rbMedium, rbLow;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;

    private TaskViewModel taskViewModel;
    private long taskId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        initViews();
        setupViewModel();
        checkEditMode();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        tilTitle = findViewById(R.id.til_title);
        tilDescription = findViewById(R.id.til_description);
        rgPriority = findViewById(R.id.rg_priority);
        rbHigh = findViewById(R.id.rb_high);
        rbMedium = findViewById(R.id.rb_medium);
        rbLow = findViewById(R.id.rb_low);
        btnSave = findViewById(R.id.btn_save);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewModel() {
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
    }

    private void checkEditMode() {
        if (getIntent().hasExtra(EXTRA_TASK_ID)) {
            isEditMode = true;
            taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);

            toolbar.setTitle("Editar Tarefa");
            btnSave.setText("Atualizar");

            // Preenche os campos com os dados da tarefa
            String title = getIntent().getStringExtra(EXTRA_TASK_TITLE);
            String description = getIntent().getStringExtra(EXTRA_TASK_DESCRIPTION);
            String priority = getIntent().getStringExtra(EXTRA_TASK_PRIORITY);

            etTitle.setText(title);
            etDescription.setText(description);

            // Seleciona a prioridade correta
            switch (priority) {
                case "HIGH":
                    rbHigh.setChecked(true);
                    break;
                case "MEDIUM":
                    rbMedium.setChecked(true);
                    break;
                case "LOW":
                    rbLow.setChecked(true);
                    break;
            }
        } else {
            toolbar.setTitle("Nova Tarefa");
            btnSave.setText("Salvar");
            rbMedium.setChecked(true); // Prioridade padrão
        }
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveTask());

        // Remove erro do título quando o usuário digita
        etTitle.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilTitle.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validação do título
        if (title.isEmpty()) {
            tilTitle.setError("Título é obrigatório");
            return;
        }

        // Obtém a prioridade selecionada
        TaskPriority priority = TaskPriority.MEDIUM;
        int selectedId = rgPriority.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_high) {
            priority = TaskPriority.HIGH;
        } else if (selectedId == R.id.rb_low) {
            priority = TaskPriority.LOW;
        }

        if (isEditMode) {
            // Atualiza tarefa existente
            Task task = new Task(title, description, priority);
            task.setId(taskId);
            taskViewModel.update(task);
            Toast.makeText(this, "Tarefa atualizada com sucesso", Toast.LENGTH_SHORT).show();
        } else {
            // Cria nova tarefa
            Task newTask = new Task(title, description, priority);
            taskViewModel.insert(newTask);
            Toast.makeText(this, "Tarefa criada com sucesso", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}