package com.todotalk.chatbot.service;

import com.todotalk.chatbot.bot.TodoTalkBot;
import com.todotalk.chatbot.model.Task;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
public class DeadlineService {

    private final TaskService taskService;
    private final TodoTalkBot bot;

    public DeadlineService(TaskService taskService, TodoTalkBot bot) {
        this.taskService = taskService;
        this.bot = bot;
    }

    @Scheduled(fixedRate = 60000) // Kiểm tra mỗi phút
    public void checkOverdueTasks() {
        // Lấy tất cả users có tasks
        taskService.getAllUserTasks().forEach((chatId, tasks) -> {
            for (Task task : tasks) {
                if (task.isOverdue() && !task.isDeadlineNotified()) {
                    sendDeadlineNotification(chatId, task);
                    task.setDeadlineNotified(true);
                }
            }
        });
    }

    private void sendDeadlineNotification(long chatId, Task task) {
        String userName = taskService.getUserName(chatId);
        String message = "⏰ **THÔNG BÁO QUÁ HẠN, " + userName + "!** ⏰\n\n" +
                        "🔴 Task \"" + task.getTitle() + "\" đã quá hạn!\n" +
                        "⏰ Deadline: " + (task.getDeadline() != null ? task.getDeadline().toString() : "Không xác định") + "\n\n" +
                        "❓ **Bạn đã hoàn thành task này chưa?**\n\n" +
                        "Phản hồi:\n" +
                        "• `/donetask " + task.getTitle().toLowerCase() + "` - Nếu đã xong\n" +
                        "• `/progress` - Xem tiến độ hiện tại\n" +
                        "• `/listtasks` - Xem tất cả tasks\n\n" +
                        "💪 Đừng nản chí! Hãy hoàn thành nó ngay bây giờ!";

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        sendMessage.setParseMode("Markdown");

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Error sending deadline notification: " + e.getMessage());
        }
    }
}
