package com.example.taskflow.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskflow.AddEditTaskActivity;
import com.example.taskflow.R;
import com.example.taskflow.data.entity.Task;
import android.widget.CheckBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnTaskActionListener listener;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnTaskActionListener {
        void onTaskCompleteToggle(Task task);
        void onTaskDelete(Task task);
        void onTaskEdit(Task task);
    }

    public TaskAdapter(Context context) {
        this.context = context;
    }

    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskTitle, tvTaskDescription, tvTaskDate;
        private CheckBox checkboxCompleted;
        private ImageButton btnMenu;
        private View priorityIndicator;

        // Variável para controlar se estamos programaticamente alterando o checkbox
        private boolean isUpdatingCheckbox = false;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvTaskDate = itemView.findViewById(R.id.tv_task_date);
            checkboxCompleted = itemView.findViewById(R.id.checkbox_completed);
            btnMenu = itemView.findViewById(R.id.btn_menu);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);

            // Configurar listener do checkbox UMA VEZ APENAS no construtor
            setupCheckboxListener();
        }

        private void setupCheckboxListener() {
            checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Ignorar se estamos atualizando programaticamente
                if (isUpdatingCheckbox) {
                    return;
                }

                // Verificar se temos uma posição válida
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < tasks.size()) {
                    Task task = tasks.get(position);
                    if (listener != null) {
                        listener.onTaskCompleteToggle(task);
                    }
                }
            });
        }

        public void bind(Task task) {
            // Configurar textos
            tvTaskTitle.setText(task.getTitle());

            // Mostra ou esconde descrição
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                tvTaskDescription.setText(task.getDescription());
                tvTaskDescription.setVisibility(View.VISIBLE);
            } else {
                tvTaskDescription.setVisibility(View.GONE);
            }

            // Define a data
            if (task.isCompleted() && task.getCompletedAt() != null) {
                tvTaskDate.setText("Concluída em: " + dateFormat.format(task.getCompletedAt()));
            } else {
                tvTaskDate.setText("Criada em: " + dateFormat.format(task.getCreatedAt()));
            }

            // IMPORTANTE: Atualizar checkbox SEM disparar o listener
            isUpdatingCheckbox = true;
            checkboxCompleted.setChecked(task.isCompleted());
            isUpdatingCheckbox = false;

            // Aplicar efeitos visuais para tarefas concluídas
            applyCompletedStyle(task.isCompleted());

            // Define cor do indicador de prioridade
            int priorityColor;
            switch (task.getPriority()) {
                case HIGH:
                    priorityColor = itemView.getContext().getColor(R.color.priority_high);
                    break;
                case LOW:
                    priorityColor = itemView.getContext().getColor(R.color.priority_low);
                    break;
                default:
                    priorityColor = itemView.getContext().getColor(R.color.priority_medium);
            }
            priorityIndicator.setBackgroundColor(priorityColor);

            // Remover listeners anteriores do botão de menu para evitar acúmulo
            btnMenu.setOnClickListener(null);
            btnMenu.setOnClickListener(v -> showPopupMenu(v, task));

            // Remover listeners anteriores do item para evitar acúmulo
            itemView.setOnLongClickListener(null);
            itemView.setOnLongClickListener(v -> {
                openEditActivity(task);
                return true;
            });
        }

        private void applyCompletedStyle(boolean isCompleted) {
            if (isCompleted) {
                // Aplicar efeito de tarefa concluída
                itemView.setAlpha(0.7f);
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                if (tvTaskDescription.getVisibility() == View.VISIBLE) {
                    tvTaskDescription.setPaintFlags(tvTaskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
            } else {
                // Remover efeito de tarefa concluída
                itemView.setAlpha(1.0f);
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                if (tvTaskDescription.getVisibility() == View.VISIBLE) {
                    tvTaskDescription.setPaintFlags(tvTaskDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }
        }

        private void showPopupMenu(View view, Task task) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.inflate(R.menu.task_menu);

            // Ajusta texto do menu baseado no status
            popup.getMenu().findItem(R.id.action_toggle_status)
                    .setTitle(task.isCompleted() ? "Marcar como pendente" : "Marcar como concluída");

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    openEditActivity(task);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (listener != null) {
                        listener.onTaskDelete(task);
                    }
                    return true;
                } else if (itemId == R.id.action_toggle_status) {
                    if (listener != null) {
                        listener.onTaskCompleteToggle(task);
                    }
                    return true;
                }
                return false;
            });

            popup.show();
        }

        private void openEditActivity(Task task) {
            Intent intent = new Intent(context, AddEditTaskActivity.class);
            intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.getId());
            intent.putExtra(AddEditTaskActivity.EXTRA_TASK_TITLE, task.getTitle());
            intent.putExtra(AddEditTaskActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
            intent.putExtra(AddEditTaskActivity.EXTRA_TASK_PRIORITY, task.getPriority().name());
            context.startActivity(intent);
        }
    }
}