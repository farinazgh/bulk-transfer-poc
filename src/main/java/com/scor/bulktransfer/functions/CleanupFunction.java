package com.scor.bulktransfer.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.scor.bulktransfer.services.CleanupService;

import java.time.OffsetDateTime;

public class CleanupFunction {

    private final CleanupService cleanupService = new CleanupService();

    @FunctionName("CleanupExpiredEntries")
    // once a day at midnight UTC time
    public void run(
            @TimerTrigger(name = "cleanupTimer", schedule = "0 0 0 * * *") String timerInfo,
            final ExecutionContext context) {

        context.getLogger().info("CleanupExpiredEntries function executed at: " + OffsetDateTime.now());

        try {
            int deletedCount = cleanupService.deleteExpiredEntries(context);
            context.getLogger().info("Deleted " + deletedCount + " expired entries from Azure Table Storage.");
        } catch (Exception e) {
            context.getLogger().severe("Error during cleanup: " + e.getMessage());
            // todo handle
        }
    }
}
