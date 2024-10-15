package com.scor.bulktransfer.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.scor.bulktransfer.services.MessagingService;
import com.scor.bulktransfer.models.EventSchema;
import com.scor.bulktransfer.services.JsonService;
import com.scor.bulktransfer.services.MetadataService;
import com.scor.bulktransfer.services.StorageService;

import java.util.Map;

/**
 * Azure Function triggered by Event Grid.
 */
public class EventGridMetadataLoggerFunction {

    private static final MetadataService metadataService = MetadataService.getInstance();
    private static final JsonService jsonService = JsonService.getInstance();
    private static final StorageService storageService = StorageService.getInstance();
    private static final MessagingService messagingService = MessagingService.getInstance();

    @FunctionName("EventGridListener")
    public void run(@EventGridTrigger(name = "event") EventSchema event, final ExecutionContext context) {

        context.getLogger().info("EventGridListener function triggered.");

        try {
            Map<String, Object> metadata = metadataService.createMetadata(event);
            String metadataJson = jsonService.convertToJson(metadata);
            storageService.logDataToTableStorage(metadataJson, event.id, context);
            messagingService.sendMessage(metadataJson, context);
        } catch (Exception e) {
            context.getLogger().severe("Error processing EventGrid event: " + e.getMessage());
            // todo rethrow or handle
        }
    }
}
