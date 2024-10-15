package com.scor.bulktransfer.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 *  common functions.
 */
public final class Utils {

    private Utils() {
    }

    public static String extractFileNameFromUrl(String url) {
        if (url == null || !url.contains("/")) {
            return "";
        }
        return url.substring(url.lastIndexOf('/') + 1);
    }

    public static String getCurrentUtcTime() {
        return OffsetDateTime.now(ZoneOffset.UTC).toString();
    }

    public static String getExpiryTimestamp(int daysToAdd) {
        return OffsetDateTime.now(ZoneOffset.UTC).plusDays(daysToAdd).toString();
    }

    public static String getCurrentDate() {
        return OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String sanitizeForTableStorage(String key) {
        if (key == null) {
            return "";
        }
        // not allowed chars in PartitionKey and RowKey
        return key.replace("/", "")
                .replace("\\", "")
                .replace("#", "")
                .replace("?", "");
    }
}
