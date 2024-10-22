package com.scor.bulktransfer.functions;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.scor.bulktransfer.models.EventSchema;
import com.scor.bulktransfer.services.JsonService;
import com.scor.bulktransfer.services.MessagingService;
import com.scor.bulktransfer.services.MetadataService;
import com.scor.bulktransfer.services.StorageService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Azure Function triggered by Event Grid.
 */
public class EventGridMetadataLoggerFunction {

    private final StorageService storageService;
    private final QueueClient deadLetterQueueClient;

    public EventGridMetadataLoggerFunction() {
        this.storageService = StorageService.getInstance();

        // Initialize the dead-letter queue client
        String queueConnectionString = System.getenv("STORAGE_CONNECTION_STRING");
        String deadLetterQueueName = "deadletterqueue";
        this.deadLetterQueueClient = new QueueClientBuilder()
                .connectionString(queueConnectionString)
                .queueName(deadLetterQueueName)
                .buildClient();
    }

    @FunctionName("EventGridListener")
    public void run(
            @EventGridTrigger(name = "event") EventSchema event,
            final ExecutionContext context) {

        context.getLogger().info(String.format("EventGridListener function triggered for Event ID: %s", event.id));

        if (event.id == null || event.id.isEmpty()) {
            context.getLogger().severe("Event ID is missing.");
            // Optionally log the event to dead-letter storage
            sendToDeadLetter(event, "Event ID is missing.", context);
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
            context.getLogger().severe(getStackTraceAsString(e));

            // Send the failed event to dead-letter storage
            sendToDeadLetter(event, e.getMessage(), context);

            // Do not rethrow the exception to prevent Event Grid from retrying
        }
    }

    private void sendToDeadLetter(EventSchema event, String errorMessage, ExecutionContext context) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String eventJson = objectMapper.writeValueAsString(event);

            // Create a dead-letter message with error details
            String deadLetterMessage = objectMapper.writeValueAsString(
                    Map.of(
                            "errorMessage", errorMessage,
                            "failedEvent", eventJson
                    )
            );

            // Send the message to the dead-letter queue
            deadLetterQueueClient.sendMessage(Base64.getEncoder().encodeToString(deadLetterMessage.getBytes(StandardCharsets.UTF_8)));

            context.getLogger().info("Event sent to dead-letter queue.");

        } catch (Exception ex) {
            context.getLogger().severe("Failed to send event to dead-letter queue: " + ex.getMessage());
            context.getLogger().severe(getStackTraceAsString(ex));
        }
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
