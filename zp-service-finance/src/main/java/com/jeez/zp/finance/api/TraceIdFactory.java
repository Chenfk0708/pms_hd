package com.jeez.zp.finance.api;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public final class TraceIdFactory {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", Locale.ROOT);

    private TraceIdFactory() {
    }

    public static String next(String scene) {
        String timestamp = OffsetDateTime.now().format(FORMATTER);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return normalizeScene(scene) + "-" + timestamp + "-" + suffix;
    }

    private static String normalizeScene(String scene) {
        if (scene == null || scene.isBlank()) {
            return "trace";
        }
        return scene.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
}
