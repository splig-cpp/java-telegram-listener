package com.example.telegram;

import java.util.Objects;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Long-polling bot that dumps every received update to the console.
 */
public class TelegramTriggerBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final UpdatePrinter printer;

    public TelegramTriggerBot(String botToken, String botUsername, UpdatePrinter printer) {
        this.botToken = Objects.requireNonNull(botToken, "botToken");
        this.botUsername = Objects.requireNonNull(botUsername, "botUsername");
        this.printer = Objects.requireNonNull(printer, "printer");
    }

    @Override
    public void onUpdateReceived(Update update) {
        printer.print(update);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
