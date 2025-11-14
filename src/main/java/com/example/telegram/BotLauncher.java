package com.example.telegram;

import java.nio.file.Path;
import java.util.Optional;
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

        UpdatePrinter printer = buildPrinter();
        TelegramTriggerBot bot = new TelegramTriggerBot(token, username, printer);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);

        System.out.printf("Bot %s is up. Waiting for updates...%n", username);

        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));
        latch.await();
    }

    private static UpdatePrinter buildPrinter() {
        UpdatePrinter.Format format = parseFormat(readOptionalConfig("TELEGRAM_OUTPUT_FORMAT"));
        Path logFile = readOptionalConfig("TELEGRAM_LOG_FILE")
                .map(Path::of)
                .orElse(null);
        return new UpdatePrinter(format, logFile);
    }

    private static UpdatePrinter.Format parseFormat(Optional<String> rawFormat) {
        if (rawFormat.isEmpty()) {
            return UpdatePrinter.Format.TEXT;
        }
        try {
            return UpdatePrinter.Format.fromString(rawFormat.get());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Unsupported TELEGRAM_OUTPUT_FORMAT value: " + rawFormat.get(), ex);
        }
    }

    private static Optional<String> readOptionalConfig(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return Optional.ofNullable(value).map(String::trim).filter(s -> !s.isEmpty());
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
