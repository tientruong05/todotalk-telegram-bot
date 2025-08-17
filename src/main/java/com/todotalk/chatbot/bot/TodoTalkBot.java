package com.todotalk.chatbot.bot;

import com.todotalk.chatbot.config.BotConfig;
import com.todotalk.chatbot.model.Task;
import com.todotalk.chatbot.service.TaskService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class TodoTalkBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final TaskService taskService;

    public TodoTalkBot(BotConfig config, TaskService taskService) {
        super(config.getBotToken());
        this.config = config;
        this.taskService = taskService;
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String telegramUsername = update.getMessage().getFrom().getUserName();
            String displayName = (telegramUsername != null && !telegramUsername.isBlank()) ? "@" + telegramUsername : firstName;
            taskService.registerUserName(chatId, displayName);

            if (messageText.startsWith("/addtask")) {
                addTaskCommandReceived(chatId, messageText, displayName);
            } else if (messageText.startsWith("/donetask")) {
                doneTaskCommandReceived(chatId, messageText, displayName);
            } else if (messageText.equals("/donealltask")) {
                doneAllTaskCommandReceived(chatId, displayName);
            } else if (messageText.equals("/cleartasks")) {
                clearTasksCommandReceived(chatId, displayName);
            } else if (messageText.startsWith("/undotask")) {
                undoTaskCommandReceived(chatId, messageText, displayName);
            } else if (messageText.equals("/progress")) {
                progressCommandReceived(chatId, displayName);
            } else {
                switch (messageText) {
                    case "/start":
                        startCommandReceived(chatId, displayName);
                        break;
                    case "/help":
                        helpCommandReceived(chatId);
                        break;
                    case "/listtasks":
                        listTodosCommandReceived(chatId, displayName);
                        break;
                    default:
                        defaultMessageReceived(chatId, messageText, displayName);
                        break;
                }
            }
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Xin chào " + name + "! 👋\n\n" +
                "Tôi là TodoTalk Bot - trợ lý quản lý công việc của bạn! 📝\n\n" +
                "Các lệnh bạn có thể sử dụng:\n" +
                "/help - Xem danh sách lệnh\n" +
                "/addtask - Thêm task với deadline và subtasks\n" +
                "/donetask - Đánh dấu hoàn thành một subtask\n" +
                "/undotask - Hoàn tác đánh dấu hoàn thành\n" +
                "/donealltask - Hoàn thành tất cả tasks\n" +
                "/progress - Xem tiến độ công việc\n" +
                "/cleartasks - Xóa tất cả tasks\n" +
                "/listtasks - Xem danh sách công việc\n\n" +
                "Hãy bắt đầu quản lý công việc hiệu quả cùng tôi! ✨";
        sendMessage(chatId, answer);
    }

    private void helpCommandReceived(long chatId) {
        String answer = "📋 **Danh sách lệnh TodoTalk Bot:**\n\n" +
                "/start - Khởi động bot\n" +
                "/help - Hiển thị trợ giúp\n" +
                "/addtask - Thêm task mới với deadline và subtasks\n" +
                "/donetask [mô tả] - Đánh dấu hoàn thành subtask\n" +
                "/undotask [mô tả] - Hoàn tác đánh dấu hoàn thành\n" +
                "/donealltask - Hoàn thành tất cả tasks\n" +
                "/progress - Xem tiến độ tổng quan\n" +
                "/cleartasks - Xóa tất cả tasks\n" +
                "/listtasks - Xem tất cả công việc\n\n" +
                "💡 **Hướng dẫn sử dụng:**\n\n" +
                "**1. Thêm task:**\n" +
                "`/addtask Hôm nay tôi sẽ hoàn thành các công việc vào lúc 5h:\n" +
                "- Code xong class 1\n" +
                "- Code xong class 2`\n\n" +
                "**📝 Hoặc task đơn giản:**\n" +
                "`/addtask chơi không đến 6h`\n\n" +
                "**2. Hoàn thành subtask:**\n" +
                "`/donetask code xong class 1`\n\n" +
                "**🎯 Hoàn thành cả task:**\n" +
                "`/donetask [tên task chính]` - Done toàn bộ task\n\n" +
                "**3. Hoàn tác subtask:**\n" +
                "`/undotask code xong class 1`\n\n" +
                "**4. Xem tiến độ:**\n" +
                "`/progress`\n\n" +
                "**5. Hoàn thành tất cả:**\n" +
                "`/donealltask`\n\n" +
                "⏰ **Tính năng mới:** Bot sẽ tự động nhắc nhở khi task quá hạn!\n" +
                "🎯 **Mẹo:** Dùng tên task chính để done/undo toàn bộ task!\n\n" +
                "Chúc bạn làm việc hiệu quả! 🚀";
        sendMessage(chatId, answer);
    }

    private void addTaskCommandReceived(long chatId, String messageText, String userName) {
        String content = messageText.replaceFirst("^/addtask\\s*", "").trim();
        if (content.isEmpty()) {
            String answer = "❌ " + userName + ", **vui lòng cung cấp tiêu đề task:**\n\n" +
                    "**Format đúng:**\n" +
                    "`/addtask [Tiêu đề task] [thời gian]`\n\n" +
                    "**Ví dụ:**\n" +
                    "• `/addtask học bài vào lúc 20h`\n" +
                    "• `/addtask chơi không đến 6h`\n" +
                    "• `/addtask Hoàn thành công việc vào lúc 17h:\n" +
                    "- Code class A\n" +
                    "- Review code\n" +
                    "- Test project`\n\n" +
                    "💡 Hãy thử lại với tiêu đề task cụ thể!";
            sendMessage(chatId, answer);
            return;
        }
        try {
            Task task = taskService.parseAndCreateTask(messageText, chatId);
            String answer = "✅ " + userName + ", **bạn đã tạo task mới:**\n\n" + task.getFormattedTask();
            if (task.getSubTasks().size() == 1 && task.getSubTasks().get(0).getDescription().equals(task.getTitle())) {
                answer += "\n📝 *Task này không có subtask nhỏ - được coi là một công việc đơn lẻ.*\n" +
                        "💡 Dùng `/donetask " + task.getTitle().toLowerCase() + "` để đánh dấu hoàn thành!";
            } else {
                answer += "\n💡 Dùng `/donetask [mô tả]` để đánh dấu hoàn thành từng subtask!";
                answer += "\n💡 Hoặc dùng `/donetask " + task.getTitle().toLowerCase() + "` để đánh dấu hoàn thành toàn bộ task!";
            }
            if (task.getDeadline() != null) {
                answer += "\n⏰ Bot sẽ nhắc nhở bạn khi đến deadline!";
            }
            sendMessage(chatId, answer);
        } catch (Exception e) {
            String answer = "❌ " + userName + ", **lỗi khi tạo task:**\n\n" +
                    "Vui lòng kiểm tra format:\n" +
                    "`/addtask Tiêu đề task vào lúc [thời gian]:\n" +
                    "- Subtask 1\n" +
                    "- Subtask 2`\n\n" +
                    "Hoặc đơn giản:\n" +
                    "`/addtask chơi không đến 6h`\n\n" +
                    "Ví dụ:\n" +
                    "`/addtask Hoàn thành công việc vào lúc 17h:\n" +
                    "- Code class A\n" +
                    "- Review code`";
            sendMessage(chatId, answer);
        }
    }

    private void doneTaskCommandReceived(long chatId, String messageText, String userName) {
        String taskDescription = messageText.replaceFirst("^/donetask\\s+", "").trim();
        if (taskDescription.isEmpty()) {
            String answer = "❌ " + userName + ", **vui lòng cung cấp mô tả task:**\n\n" +
                    "**Format đúng:**\n" +
                    "• `/donetask [tên subtask]` - Hoàn thành 1 subtask\n" +
                    "• `/donetask [tên task chính]` - Hoàn thành toàn bộ task\n\n" +
                    "**Ví dụ:**\n" +
                    "• `/donetask code xong class 1`\n" +
                    "• `/donetask học bài`\n\n" +
                    "💡 Dùng `/listtasks` để xem danh sách tasks hiện có!";
            sendMessage(chatId, answer);
            return;
        }
        boolean success = taskService.markTaskDone(taskDescription, chatId);
        if (success) {
            Task foundTask = taskService.findTaskByName(taskDescription, chatId);
            boolean isDoneWholeTask = (foundTask != null && foundTask.isAllCompleted());
            String answer;
            if (isDoneWholeTask && foundTask != null) {
                answer = "✅ Tuyệt vời " + userName + "! **Đã hoàn thành toàn bộ task:** \"" + foundTask.getTitle() + "\"\n\n" +
                        "🎉 Tất cả subtasks đã được đánh dấu hoàn thành!\n" +
                        "Dùng `/listtasks` để xem tiến độ hiện tại! 📊";
            } else {
                answer = "✅ " + userName + ", **đã hoàn thành subtask:** \"" + taskDescription + "\"\n\n" +
                        "Dùng `/listtasks` để xem tiến độ hiện tại! 📊";
            }
            sendMessage(chatId, answer);
            if (taskService.hasCompletedAllTasks(chatId)) {
                String congratsMessage = "🎉 **CHÚC MỪNG " + userName + "!** 🎉\n\n" +
                        "Bạn đã hoàn thành tất cả công việc! 🏆\n" +
                        "Thật tuyệt vời! Hãy nghỉ ngơi và thưởng cho bản thân nhé! ✨";
                sendMessage(chatId, congratsMessage);
            }
        } else {
            String answer = "❌ " + userName + ", **không tìm thấy task hoặc subtask:** \"" + taskDescription + "\"\n\n" +
                    "💡 **Gợi ý:**\n" +
                    "• `/donetask [tên subtask]` - Hoàn thành 1 subtask\n" +
                    "• `/donetask [tên task chính]` - Hoàn thành toàn bộ task\n" +
                    "• `/listtasks` - Xem danh sách để kiểm tra tên chính xác";
            sendMessage(chatId, answer);
        }
    }

    private void doneAllTaskCommandReceived(long chatId, String userName) {
        List<Task> tasks = taskService.getUserTasks(chatId);
        if (tasks.isEmpty()) {
            String answer = "📝 " + userName + ", **chưa có task nào!**\n\n" +
                    "Dùng `/addtask` để thêm task mới.";
            sendMessage(chatId, answer);
            return;
        }
        taskService.markAllTasksDone(chatId);
        String answer = "🎉 **CHÚC MỪNG " + userName + "! BẠN ĐÃ HOÀN THÀNH TẤT CẢ!** 🎉\n\n" +
                "✅ Đã đánh dấu hoàn thành tất cả subtasks!\n" +
                "🏆 Bạn thật xuất sắc!\n" +
                "🌟 Hãy nghỉ ngơi và thưởng cho bản thân nhé!\n\n" +
                "Dùng `/addtask` để thêm công việc mới! 💪";
        sendMessage(chatId, answer);
    }

    private void undoTaskCommandReceived(long chatId, String messageText, String userName) {
        String taskDescription = messageText.replaceFirst("^/undotask\\s+", "").trim();
        if (taskDescription.isEmpty()) {
            String answer = "❌ " + userName + ", **vui lòng cung cấp mô tả task cần hoàn tác:**\n\n" +
                    "**Format đúng:**\n" +
                    "• `/undotask [tên subtask]` - Hoàn tác 1 subtask\n" +
                    "• `/undotask [tên task chính]` - Hoàn tác toàn bộ task\n\n" +
                    "**Ví dụ:**\n" +
                    "• `/undotask code xong class 1`\n" +
                    "• `/undotask học bài`\n\n" +
                    "💡 Chỉ có thể hoàn tác những task/subtask đã hoàn thành!";
            sendMessage(chatId, answer);
            return;
        }
        boolean success = taskService.undoTaskDone(taskDescription, chatId);
        if (success) {
            Task foundTask = taskService.findTaskByName(taskDescription, chatId);
            boolean isUndoWholeTask = (foundTask != null && !foundTask.isAllCompleted());
            String answer;
            if (isUndoWholeTask && foundTask != null) {
                answer = "↩️ " + userName + ", **đã hoàn tác toàn bộ task:** \"" + foundTask.getTitle() + "\"\n\n" +
                        "📝 Tất cả subtasks đã được đánh dấu chưa hoàn thành.\n" +
                        "Dùng `/listtasks` để xem tiến độ hiện tại!";
            } else {
                answer = "↩️ " + userName + ", **đã hoàn tác subtask:** \"" + taskDescription + "\"\n\n" +
                        "📝 Subtask này đã được đánh dấu chưa hoàn thành.\n" +
                        "Dùng `/listtasks` để xem tiến độ hiện tại!";
            }
            sendMessage(chatId, answer);
        } else {
            String answer = "❌ " + userName + ", **không tìm thấy task hoặc subtask đã hoàn thành:** \"" + taskDescription + "\"\n\n" +
                    "💡 **Gợi ý:**\n" +
                    "• `/undotask [tên subtask]` - Hoàn tác 1 subtask đã hoàn thành\n" +
                    "• `/undotask [tên task chính]` - Hoàn tác toàn bộ task đã hoàn thành\n" +
                    "• `/listtasks` - Xem danh sách để kiểm tra trạng thái";
            sendMessage(chatId, answer);
        }
    }

    private void progressCommandReceived(long chatId, String userName) {
        List<Task> tasks = taskService.getUserTasks(chatId);
        if (tasks.isEmpty()) {
            String answer = "📝 " + userName + ", **chưa có task nào để theo dõi tiến độ!**\n\n" +
                    "Dùng `/addtask` để thêm task mới.";
            sendMessage(chatId, answer);
            return;
        }
        int totalProgress = taskService.getTotalProgress(chatId);
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().mapToLong(task -> task.isAllCompleted() ? 1 : 0).sum();
        String progressBar = getProgressBar(totalProgress);
        String answer = "📊 **Tiến độ công việc của bạn, " + userName + ":**\n\n" +
                progressBar + " " + totalProgress + "%\n\n" +
                "📋 **Thống kê:**\n" +
                "• Tasks hoàn thành: " + completedTasks + "/" + totalTasks + "\n" +
                "• Tổng tiến độ: " + totalProgress + "%\n\n";
        if (totalProgress == 100) {
            answer += "🎉 **Chúc mừng! Bạn đã hoàn thành tất cả!** 🎉\n";
        } else if (totalProgress >= 75) {
            answer += "🔥 **Sắp xong rồi! Cố lên!** 💪\n";
        } else if (totalProgress >= 50) {
            answer += "⚡ **Đang làm rất tốt! Tiếp tục!** 👍\n";
        } else {
            answer += "🚀 **Bắt đầu thôi! Bạn làm được!** ✨\n";
        }
        sendMessage(chatId, answer);
    }

    private void clearTasksCommandReceived(long chatId, String userName) {
        List<Task> tasks = taskService.getUserTasks(chatId);
        if (tasks.isEmpty()) {
            String answer = "📝 " + userName + ", **không có task nào để xóa!**\n\n" +
                    "Dùng `/addtask` để thêm task mới.";
            sendMessage(chatId, answer);
            return;
        }
        taskService.clearUserTasks(chatId);
        String answer = "🗑️ " + userName + ", **đã xóa tất cả tasks!**\n\n" +
                "Tất cả công việc đã được xóa khỏi danh sách.\n" +
                "Dùng `/addtask` để bắt đầu lại! 🚀";
        sendMessage(chatId, answer);
    }

    private String getProgressBar(int percentage) {
        int filledBars = percentage / 10;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < filledBars) {
                bar.append("🟩");
            } else {
                bar.append("⬜");
            }
        }
        return bar.toString();
    }

    private void listTodosCommandReceived(long chatId, String userName) {
        List<Task> tasks = taskService.getUserTasks(chatId);
        if (tasks.isEmpty()) {
            String answer = "📝 " + userName + ", **chưa có task nào!**\n\n" +
                    "Dùng `/addtask` để thêm task mới.\n\n" +
                    "Ví dụ:\n" +
                    "`/addtask Hoàn thành công việc vào lúc 17h:\n" +
                    "- Code class A\n" +
                    "- Review code`";
            sendMessage(chatId, answer);
            return;
        }
        StringBuilder answer = new StringBuilder("📋 **Danh sách Tasks của bạn, " + userName + ":**\n\n");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            answer.append("**Task ").append(i + 1).append(":**\n");
            answer.append(task.getFormattedTask()).append("\n");
            if (task.isAllCompleted()) {
                answer.append("🎉 *Đã hoàn thành!* 🎉\n");
            }
            answer.append("\n");
        }
        answer.append("💡 Dùng `/donetask [mô tả]` để hoàn thành subtask!");
        sendMessage(chatId, answer.toString());
    }

    private void defaultMessageReceived(long chatId, String messageText, String userName) {
        String answer = "🤔 " + userName + ", **lệnh này chưa được hỗ trợ!**\n\n" +
                "Dùng `/help` để xem danh sách lệnh có sẵn.\n\n" +
                "🔍 **Lệnh phổ biến:**\n" +
                "• `/addtask` - Thêm task mới\n" +
                "• `/listtasks` - Xem danh sách tasks\n" +
                "• `/progress` - Xem tiến độ\n" +
                "• `/donetask [mô tả]` - Hoàn thành subtask\n" +
                "• `/undotask [mô tả]` - Hoàn tác subtask";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }
}
