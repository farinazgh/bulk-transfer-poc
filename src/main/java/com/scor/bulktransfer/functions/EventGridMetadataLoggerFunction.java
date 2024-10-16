package com.scor.bulktransfer.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.scor.bulktransfer.models.EventSchema;
import com.scor.bulktransfer.services.JsonService;
import com.scor.bulktransfer.services.MessagingService;
import com.scor.bulktransfer.services.MetadataService;
import com.scor.bulktransfer.services.StorageService;

import java.util.Map;

/**
 * Azure Function triggered by Event Grid.
 */
public class EventGridMetadataLoggerFunction {

    private final MetadataService metadataService;
    private final JsonService jsonService;
    private final StorageService storageService;
    private final MessagingService messagingService;

    // singleton for production
    public EventGridMetadataLoggerFunction() {
        this.metadataService = MetadataService.getInstance();
        this.jsonService = JsonService.getInstance();
        this.storageService = StorageService.getInstance();
        this.messagingService = MessagingService.getInstance();
    }

    //  for testing and injecting mock instances
    public EventGridMetadataLoggerFunction(MetadataService metadataService, JsonService jsonService, StorageService storageService, MessagingService messagingService) {
        this.metadataService = metadataService;
        this.jsonService = jsonService;
        this.storageService = storageService;
        this.messagingService = messagingService;
    }

    @FunctionName("EventGridListener")
    public void run(@EventGridTrigger(name = "event") EventSchema event, final ExecutionContext context) {

        context.getLogger().info(String.format("EventGridListener function triggered for Event ID: %s", event.id));

        if (event.id == null || event.id.isEmpty()) {
            context.getLogger().severe("Event ID is missing.");
            throw new IllegalArgumentException("Event ID is required.");
        }

        try {
            if (storageService.isEventProcessed(event.id)) {
                context.getLogger().info(String.format("Event already processed: %s", event.id));
                return;
            }

            Map<String, Object> metadata = metadataService.createMetadata(event);
            String metadataJson = jsonService.convertToJson(metadata);
            storageService.logDataToTableStorage(metadataJson, event.id, context);
            messagingService.sendMessage(metadataJson, context);
            context.getLogger().info(String.format("Successfully processed Event ID: %s", event.id));

        } catch (Exception e) {
            context.getLogger().severe(String.format("Error processing EventGrid event ID %s: %s", event.id, e.getMessage()));
            throw e;
        }
    }
}
