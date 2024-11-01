package com.superstefo.automatic.investor.services;

import java.util.Objects;

public class OneLineLogger {
    public static int MAX_LENGTH = 110;
    private int logLength = 0;

    public void print(String title, String message) {
        title = Objects.requireNonNullElse(title, "title");
        message = Objects.requireNonNullElse(message, "message");
        if (logLength == 0) {
            System.out.print(title);
            logLength = logLength + title.length();
        }
        System.out.print(" " + message);
        logLength = logLength + message.length() + 1;

        if (logLength >= MAX_LENGTH) {
            System.out.print("\n");
            logLength = 0;
        }
    }

    public static OneLineLogger create() {
        return new OneLineLogger();
    }
}
