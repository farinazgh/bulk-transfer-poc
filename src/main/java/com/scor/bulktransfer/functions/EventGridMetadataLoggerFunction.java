package com.scor.bulktransfer.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.scor.bulktransfer.models.EventSchema;
import com.scor.bulktransfer.services.*;

import java.util.Map;

/**
 * Azure Function triggered by Event Grid.
 */
public class EventGridMetadataLoggerFunction {

    private final StorageService storageService;

    public EventGridMetadataLoggerFunction() {
        this.storageService = StorageService.getInstance();

    }

    @FunctionName("EventGridListener")
    public void run(
            @EventGridTrigger(name = "event") EventSchema event,
            final ExecutionContext context) {

        context.getLogger().info(String.format("EventGridListener function triggered for Event ID: %s", event.id));

        if (event.id == null || event.id.isEmpty()) {
            context.getLogger().severe("Event ID is missing.");
            // Optionally log the event to dead-letter storage
            DeadLetterQueueService.sendToDeadLetter(event, "Event ID is missing.", context);
            return;
        }

        try {
            if (storageService.isEventProcessed(event.id)) {
                context.getLogger().info(String.format("Event already processed: %s", event.id));
                return;
            }

            Map<String, Object> metadata = MetadataService.createMetadata(event);
            String metadataJson = JsonService.convertToJson(metadata);

            storageService.logDataToTableStorage(metadataJson, event.id, context);
            MessagingService.sendMessageToServiceBus(metadata, context);

            storageService.markEventAsProcessed(event.id);

            context.getLogger().info(String.format("Successfully processed Event ID: %s", event.id));

        } catch (Exception e) {
            context.getLogger().severe(String.format("Error processing EventGrid event ID %s: %s", event.id, e.getMessage()));
            context.getLogger().severe(DeadLetterQueueService.getStackTraceAsString(e));

            // failed event to dead-letter
            DeadLetterQueueService.sendToDeadLetter(event, e.getMessage(), context);

            // todo Does not rethrowing the exception prevent Event Grid from retrying?
        }
    }
}

