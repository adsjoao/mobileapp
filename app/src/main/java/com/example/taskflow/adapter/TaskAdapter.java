package com.example.taskflow.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import com.example.taskflow.data.entity.Task;
import com.example.taskflow.data.entity.TaskPriority;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskflow.AddEditTaskActivity;
import com.example.taskflow.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnTaskActionListener listener;
    private Context context;
    private SimpleDateFormat dateFormat;

    public interface OnTaskActionListener {
        void onTaskCompleteToggle(Task task);
        void onTaskDelete(Task task);
        void onTaskEdit(Task task);
    }

    public TaskAdapter(Context context) {
        this.context = context;
        // Configurar formatador de data para fuso horário brasileiro
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
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

    public void setTasks(List<Task> newTasks) {
        if (newTasks == null) {
            newTasks = new ArrayList<>();
        }

        // Usar DiffUtil para atualizações mais eficientes
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TaskDiffCallback(this.tasks, newTasks));
        this.tasks.clear();
        this.tasks.addAll(newTasks);
        diffResult.dispatchUpdatesTo(this);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskTitle, tvTaskDescription, tvTaskDate;
        private CheckBox checkboxCompleted;
        private ImageButton btnMenu;
        private View priorityIndicator;
        private boolean isBinding = false; // Flag para evitar loops

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvTaskDate = itemView.findViewById(R.id.tv_task_date);
            checkboxCompleted = itemView.findViewById(R.id.checkbox_completed);
            btnMenu = itemView.findViewById(R.id.btn_menu);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
        }

        public void bind(Task task) {
            isBinding = true; // Sinaliza que estamos fazendo bind

            tvTaskTitle.setText(task.getTitle());

            // Mostra ou esconde descrição
            if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
                tvTaskDescription.setText(task.getDescription());
                tvTaskDescription.setVisibility(View.VISIBLE);
            } else {
                tvTaskDescription.setVisibility(View.GONE);
            }

            // Define a data com fuso horário correto
            if (task.isCompleted() && task.getCompletedAt() != null) {
                tvTaskDate.setText("Concluída em: " + dateFormat.format(task.getCompletedAt()));
            } else if (task.getCreatedAt() != null) {
                tvTaskDate.setText("Criada em: " + dateFormat.format(task.getCreatedAt()));
            } else {
                tvTaskDate.setText("Data não disponível");
            }

            // Define status de completado SEM disparar listener
            checkboxCompleted.setOnCheckedChangeListener(null);
            checkboxCompleted.setChecked(task.isCompleted());

            // Ajusta visual para tarefas concluídas
            updateTaskVisualState(task);

            // Define cor do indicador de prioridade
            updatePriorityIndicator(task);

            isBinding = false; // Finaliza o bind

            // Agora define o listener do checkbox
            checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isBinding && listener != null) {
                    listener.onTaskCompleteToggle(task);
                }
            });

            // Listener alternativo para clique no checkbox (mais confiável)
            checkboxCompleted.setOnClickListener(v -> {
                if (!isBinding && listener != null) {
                    listener.onTaskCompleteToggle(task);
                }
            });

            // Listener para o menu
            btnMenu.setOnClickListener(v -> showPopupMenu(v, task));

            // Listener para clique longo no item (edição rápida)
            itemView.setOnLongClickListener(v -> {
                openEditActivity(task);
                return true;
            });
        }

        private void updateTaskVisualState(Task task) {
            float alpha = task.isCompleted() ? 0.6f : 1.0f;

            // Aplicar transparência
            itemView.setAlpha(alpha);

            // Aplicar strikethrough no título se concluída
            if (task.isCompleted()) {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }

        private void updatePriorityIndicator(Task task) {
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

    // Classe para DiffUtil melhorar performance
    private static class TaskDiffCallback extends DiffUtil.Callback {
        private final List<Task> oldList;
        private final List<Task> newList;

        public TaskDiffCallback(List<Task> oldList, List<Task> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Task oldTask = oldList.get(oldItemPosition);
            Task newTask = newList.get(newItemPosition);
            return oldTask.getId() == newTask.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Task oldTask = oldList.get(oldItemPosition);
            Task newTask = newList.get(newItemPosition);

            // Comparação mais rigorosa para detectar mudanças
            boolean titleSame = oldTask.getTitle().equals(newTask.getTitle());
            boolean completedSame = oldTask.isCompleted() == newTask.isCompleted();
            boolean prioritySame = oldTask.getPriority() == newTask.getPriority();

            boolean descriptionSame =
                    (oldTask.getDescription() == null && newTask.getDescription() == null) ||
                            (oldTask.getDescription() != null && oldTask.getDescription().equals(newTask.getDescription()));

            boolean completedAtSame =
                    (oldTask.getCompletedAt() == null && newTask.getCompletedAt() == null) ||
                            (oldTask.getCompletedAt() != null && newTask.getCompletedAt() != null &&
                                    oldTask.getCompletedAt().getTime() == newTask.getCompletedAt().getTime());

            return titleSame && completedSame && prioritySame && descriptionSame && completedAtSame;
        }
    }
}