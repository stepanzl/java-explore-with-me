package ru.practicum.main.util;

import java.time.format.DateTimeFormatter;

public final class DateTimeFormat {

    private DateTimeFormat() {
    }

    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN);
}