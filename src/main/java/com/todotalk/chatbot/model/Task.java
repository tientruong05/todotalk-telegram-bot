package com.todotalk.chatbot.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private String title;
    private LocalTime deadline;
    private LocalDate createdDate;
    private List<SubTask> subTasks;
    private long chatId;
    private boolean deadlineNotified;

    public Task() {
        this.subTasks = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.deadlineNotified = false;
    }

    public Task(String title, LocalTime deadline, long chatId) {
        this.title = title;
        this.deadline = deadline;
        this.chatId = chatId;
        this.subTasks = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.deadlineNotified = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalTime deadline) {
        this.deadline = deadline;
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isDeadlineNotified() {
        return deadlineNotified;
    }

    public void setDeadlineNotified(boolean deadlineNotified) {
        this.deadlineNotified = deadlineNotified;
    }

    public void addSubTask(String description) {
        this.subTasks.add(new SubTask(description));
    }

    public boolean markSubTaskDone(String description) {
        for (SubTask subTask : subTasks) {
            if (subTask.getDescription().toLowerCase().contains(description.toLowerCase())) {
                subTask.setCompleted(true);
                return true;
            }
        }
        return false;
    }
    
    public boolean undoSubTaskDone(String description) {
        for (SubTask subTask : subTasks) {
            if (subTask.getDescription().toLowerCase().contains(description.toLowerCase()) && subTask.isCompleted()) {
                subTask.setCompleted(false);
                return true;
            }
        }
        return false;
    }

    public void markAllSubTasksDone() {
        for (SubTask subTask : subTasks) {
            subTask.setCompleted(true);
        }
    }

    public boolean isAllCompleted() {
        return subTasks.stream().allMatch(SubTask::isCompleted);
    }

    public boolean isOverdue() {
        if (deadline == null) return false;
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        
        // Chỉ kiểm tra overdue cho task được tạo hôm nay
        return createdDate.equals(today) && now.isAfter(deadline) && !isAllCompleted();
    }

    public String getFormattedTask() {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 **").append(title).append("**\n");
        if (deadline != null) {
            sb.append("⏰ Deadline: ").append(deadline.toString()).append("\n\n");
        }
        
        for (SubTask subTask : subTasks) {
            if (subTask.isCompleted()) {
                sb.append("✅ ~~").append(subTask.getDescription()).append("~~\n");
            } else {
                sb.append("🔸 ").append(subTask.getDescription()).append("\n");
            }
        }
        
        return sb.toString();
    }
}
