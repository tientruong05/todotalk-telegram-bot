package com.todotalk.chatbot.service;

import com.todotalk.chatbot.model.SubTask;
import com.todotalk.chatbot.model.Task;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaskService {
    
    // Lưu trữ tasks theo chatId
    private final Map<Long, List<Task>> userTasks = new ConcurrentHashMap<>();
    // Lưu tên user theo chatId để cá nhân hóa thông báo
    private final Map<Long, String> userNames = new ConcurrentHashMap<>();

    public void registerUserName(long chatId, String name) {
        if (name != null && !name.isBlank()) {
            userNames.put(chatId, name.trim());
        }
    }

    public String getUserName(long chatId) {
        return userNames.getOrDefault(chatId, "bạn");
    }

    public Task parseAndCreateTask(String message, long chatId) {
        // Tìm thời gian deadline trong message
        LocalTime deadline = extractDeadline(message);
        
        // Tìm title (phần từ đầu đến trước thời gian hoặc dấu ":")
        String title = extractTitle(message);
        
        // Tạo task mới
        Task task = new Task(title, deadline, chatId);
        
        // Tìm và thêm các subtasks (dòng bắt đầu bằng "-")
        List<String> subTasks = extractSubTasks(message);
        for (String subTaskDesc : subTasks) {
            task.addSubTask(subTaskDesc);
        }
        
        // Nếu không có subtask nào, tạo 1 subtask mặc định từ title
        if (subTasks.isEmpty()) {
            task.addSubTask(title);
        }
        
        // Lưu task vào danh sách của user
        userTasks.computeIfAbsent(chatId, k -> new ArrayList<>()).add(task);
        
        return task;
    }
    
    public boolean markTaskDone(String taskDescription, long chatId) {
        List<Task> tasks = userTasks.get(chatId);
        if (tasks == null) return false;
        
        // Ưu tiên tìm task chính trước
        Task mainTask = findTaskByName(taskDescription, chatId);
        if (mainTask != null) {
            // Done tất cả subtasks của task này
            mainTask.markAllSubTasksDone();
            return true;
        }
        
        // Nếu không tìm thấy task chính, tìm trong subtasks
        for (Task task : tasks) {
            if (task.markSubTaskDone(taskDescription)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean undoTaskDone(String taskDescription, long chatId) {
        List<Task> tasks = userTasks.get(chatId);
        if (tasks == null) return false;
        
        // Ưu tiên tìm task chính đã hoàn thành trước
        Task mainTask = findTaskByName(taskDescription, chatId);
        if (mainTask != null && mainTask.isAllCompleted()) {
            // Undo tất cả subtasks của task này
            for (SubTask subTask : mainTask.getSubTasks()) {
                subTask.setCompleted(false);
            }
            return true;
        }
        
        // Nếu không tìm thấy task chính hoàn thành, tìm trong subtasks
        for (Task task : tasks) {
            if (task.undoSubTaskDone(taskDescription)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void markAllTasksDone(long chatId) {
        List<Task> tasks = userTasks.get(chatId);
        if (tasks != null) {
            for (Task task : tasks) {
                task.markAllSubTasksDone();
            }
        }
    }
    
    public List<Task> getUserTasks(long chatId) {
        return userTasks.getOrDefault(chatId, new ArrayList<>());
    }
    
    public Map<Long, List<Task>> getAllUserTasks() {
        return userTasks;
    }
    
    public boolean hasCompletedAllTasks(long chatId) {
        List<Task> tasks = userTasks.get(chatId);
        if (tasks == null || tasks.isEmpty()) return false;
        
        return tasks.stream().allMatch(Task::isAllCompleted);
    }
    
    public int getTotalProgress(long chatId) {
        List<Task> tasks = userTasks.get(chatId);
        if (tasks == null || tasks.isEmpty()) return 0;
        
        int totalSubTasks = 0;
        int completedSubTasks = 0;
        
        for (Task task : tasks) {
            totalSubTasks += task.getSubTasks().size();
            completedSubTasks += (int) task.getSubTasks().stream().mapToLong(subTask -> subTask.isCompleted() ? 1 : 0).sum();
        }
        
        return totalSubTasks > 0 ? (completedSubTasks * 100) / totalSubTasks : 0;
    }
    
    public void clearUserTasks(long chatId) {
        userTasks.remove(chatId);
    }
    
    public Task findTaskByName(String taskName, long chatId) {
        List<Task> tasks = userTasks.get(chatId);
        if (tasks == null) return null;
        
        for (Task task : tasks) {
            if (task.getTitle().toLowerCase().contains(taskName.toLowerCase())) {
                return task;
            }
        }
        return null;
    }
    
    private LocalTime extractDeadline(String message) {
        // Pattern để tìm thời gian: "vào lúc 5h", "lúc 17:30", "đến 5h", "trước 8h", "không đến 6h", etc.
        Pattern timePattern = Pattern.compile("(?:vào lúc|lúc|đến|trước|sau|không đến)\\s*(\\d{1,2})(?:h|:|giờ)(\\d{2})?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = timePattern.matcher(message);
        
        if (matcher.find()) {
            try {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
                
                // Validate time
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return LocalTime.of(hour, minute);
                }
            } catch (Exception e) {
                return null;
            }
        }
        
        // Thử pattern khác: "5:30", "17:00", etc.
        Pattern timePattern2 = Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\b");
        Matcher matcher2 = timePattern2.matcher(message);
        
        if (matcher2.find()) {
            try {
                int hour = Integer.parseInt(matcher2.group(1));
                int minute = Integer.parseInt(matcher2.group(2));
                
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return LocalTime.of(hour, minute);
                }
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
    
    private String extractTitle(String message) {
        // Loại bỏ "/addtask" từ đầu
        String content = message.replaceFirst("^/addtask\\s+", "").trim();
        
        // Nếu không có content sau /addtask, throw exception
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        
        // Tìm vị trí của dấu ":" hoặc pattern thời gian
        Pattern timePattern = Pattern.compile("(?:vào lúc|lúc|đến|trước|sau|không đến)\\s*\\d{1,2}(?:h|:|giờ)");
        Matcher matcher = timePattern.matcher(content);
        
        if (matcher.find()) {
            // Lấy phần trước thời gian
            return content.substring(0, matcher.start()).trim();
        } else {
            // Nếu không có thời gian, tìm dấu ":" đầu tiên
            int colonIndex = content.indexOf(":");
            if (colonIndex > 0) {
                return content.substring(0, colonIndex).trim();
            }
        }
        
        // Nếu không tìm thấy gì, lấy dòng đầu tiên hoặc toàn bộ nếu không có xuống dòng
        String[] lines = content.split("\n");
        String firstLine = lines.length > 0 ? lines[0].trim() : "Công việc mới";
        
        // Nếu dòng đầu tiên có chứa thông tin thời gian, lấy phần trước thời gian
        Pattern timeInLinePattern = Pattern.compile("(.+?)(?:vào lúc|lúc|đến|trước|sau|không đến)\\s*\\d{1,2}(?:h|:|giờ)");
        Matcher lineTimeMatcher = timeInLinePattern.matcher(firstLine);
        
        if (lineTimeMatcher.find()) {
            return lineTimeMatcher.group(1).trim();
        }
        
        return firstLine.isEmpty() ? "Công việc mới" : firstLine;
    }
    
    private List<String> extractSubTasks(String message) {
        List<String> subTasks = new ArrayList<>();
        String[] lines = message.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("-") || line.startsWith("•") || line.startsWith("*") || line.startsWith("+")) {
                // Loại bỏ dấu đầu dòng và khoảng trắng
                String subTask = line.substring(1).trim();
                if (!subTask.isEmpty()) {
                    subTasks.add(subTask);
                }
            } else if (line.matches("^\\d+\\.\\s+.*")) {
                // Pattern cho "1. Task", "2. Task", etc.
                String subTask = line.replaceFirst("^\\d+\\.\\s+", "").trim();
                if (!subTask.isEmpty()) {
                    subTasks.add(subTask);
                }
            }
        }
        
        return subTasks;
    }
}
