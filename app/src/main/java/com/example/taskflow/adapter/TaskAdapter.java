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
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskflow.AddEditTaskActivity;
import com.example.taskflow.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskTitle, tvTaskDescription, tvTaskDate;
        private CheckBox checkboxCompleted;
        private ImageButton btnMenu;
        private View priorityIndicator;

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
            tvTaskTitle.setText(task.getTitle());

            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                tvTaskDescription.setText(task.getDescription());
                tvTaskDescription.setVisibility(View.VISIBLE);
            } else {
                tvTaskDescription.setVisibility(View.GONE);
            }

            if (task.isCompleted() && task.getCompletedAt() != null) {
                tvTaskDate.setText("Concluída em: " + dateFormat.format(task.getCompletedAt()));
            } else {
                tvTaskDate.setText("Criada em: " + dateFormat.format(task.getCreatedAt()));
            }

            checkboxCompleted.setChecked(task.isCompleted());
            itemView.setAlpha(task.isCompleted() ? 0.6f : 1.0f);

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

            checkboxCompleted.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskCompleteToggle(task);
                }
            });

            btnMenu.setOnClickListener(v -> showPopupMenu(v, task));

            itemView.setOnLongClickListener(v -> {
                openEditActivity(task);
                return true;
            });
        }

        private void showPopupMenu(View view, Task task) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.task_menu);

            // FORÇA O MENU A SER SEMPRE VISÍVEL
            try {
                java.lang.reflect.Field fieldMPopup = popup.getClass().getDeclaredField("mPopup");
                fieldMPopup.setAccessible(true);
                Object mPopup = fieldMPopup.get(popup);
                mPopup.getClass().getDeclaredMethod("setForceShowIcon", boolean.class).invoke(mPopup, true);
            } catch (Exception e) {
                // Ignora se não conseguir forçar os ícones
            }

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