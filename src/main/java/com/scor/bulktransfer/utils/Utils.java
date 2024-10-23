package com.scor.bulktransfer.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * common functions.
 */
public final class Utils {

    private Utils() {
    }

    public static String extractValidUrl(Object urlObj) {
        if (urlObj == null) {
            throw new IllegalArgumentException("URL is null.");
        }
        return urlObj.toString();
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

    public static String nullSafe(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    public static String extractContainerName(String input) {
        String containerPrefix = "windows.net/";
        int startIndex = input.indexOf(containerPrefix) + containerPrefix.length();

        int endIndex = input.indexOf("/", startIndex);

        if (endIndex != -1) {
            return input.substring(startIndex, endIndex);
        }

        return null;
    }
    public static String generatePartitionKey(String eventId) {
        return eventId.length() > 5 ? eventId.substring(0, 5) : eventId;
    }
    public static String generateRowKey(String eventId) {
        return eventId;
    }
}
