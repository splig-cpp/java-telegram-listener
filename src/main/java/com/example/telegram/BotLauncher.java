package com.example.telegram;

import java.util.concurrent.CountDownLatch;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Boots the {@link TelegramTriggerBot}. Provide TELEGRAM_BOT_TOKEN and TELEGRAM_BOT_USERNAME via
 * environment variables or system properties before running.
 */
public final class BotLauncher {
    private BotLauncher() {
    }

    public static void main(String[] args) throws TelegramApiException, InterruptedException {
        String token = readConfig("TELEGRAM_BOT_TOKEN");
        String username = readConfig("TELEGRAM_BOT_USERNAME");

        TelegramTriggerBot bot = new TelegramTriggerBot(token, username, new UpdatePrinter());
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);

        System.out.printf("Bot %s is up. Waiting for updates...%n", username);

        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));
        latch.await();
    }

    private static String readConfig(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config " + key);
        }
        return value;
    }
}
