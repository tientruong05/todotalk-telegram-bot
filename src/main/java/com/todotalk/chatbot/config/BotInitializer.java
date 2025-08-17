package com.todotalk.chatbot.config;

import com.todotalk.chatbot.bot.TodoTalkBot;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    private final TodoTalkBot bot;

    public BotInitializer(TodoTalkBot bot) {
        this.bot = bot;
    }

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
            System.out.println("TodoTalk Bot đã được khởi động thành công! 🚀");
        } catch (TelegramApiException e) {
            System.err.println("Lỗi khi khởi động bot: " + e.getMessage());
        }
    }
}
