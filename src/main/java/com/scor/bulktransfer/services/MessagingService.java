package com.scor.bulktransfer.services;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.microsoft.azure.functions.ExecutionContext;
import com.scor.bulktransfer.models.EventSchema;
import com.scor.bulktransfer.models.ServiceBusMessagePayload;

/**
 * sends the message to azure servicebus
 */

public class MessagingService {
    private static final String SERVICE_BUS_CONNECTION_STRING = "bulktransfer-neu-sb-dev.servicebus.windows.net";
    private static final String SERVICE_BUS_TOPIC_NAME = "bulktransfer-blobevents-topic";

    private final ServiceBusSenderClient senderClient;

    // Singleton Instance
    private static final MessagingService INSTANCE = new MessagingService();

    private MessagingService() {
        if (SERVICE_BUS_CONNECTION_STRING == null || SERVICE_BUS_CONNECTION_STRING.isEmpty()) {
            throw new IllegalStateException("Service Bus connection string is not set.");
        }
        ;

        this.senderClient = new ServiceBusClientBuilder()
                .connectionString(SERVICE_BUS_CONNECTION_STRING)
                .sender()
                .topicName(SERVICE_BUS_TOPIC_NAME)
                .buildClient();
    }

    public static MessagingService getInstance() {
        return INSTANCE;
    }

    public void sendMessage(String messageContent, String messageId, ExecutionContext context) {
        try {
            ServiceBusMessage message = new ServiceBusMessage(messageContent)
                    .setMessageId(messageId) // Ensures uniqueness for duplicate detection
                    .setContentType("application/json");

            senderClient.sendMessage(message);
            context.getLogger().info("Message sent to Service Bus topic.");
        } catch (ServiceBusException e) {
            context.getLogger().severe("Service Bus error: " + e.getMessage());
            throw new RuntimeException("Failed to send message to Service Bus", e);
        } catch (Exception e) {
            context.getLogger().severe("Serialization error: " + e.getMessage());
            throw new RuntimeException("Failed to serialize message payload", e);
        }
    }


    public void sendEventMessage(EventSchema event, ExecutionContext context) {
        context.getLogger().info(">> == sendEventMessage: " + event);

        if (event.id == null || event.id.isEmpty()) {
            context.getLogger().severe("Event ID is missing.");
            throw new IllegalArgumentException("Event ID is required.");
        }

//        ServiceBusMessagePayload messagePayload = createMessagePayload(event);

//        String messagePayloadJson = JsonService.convertToJson(messagePayload);
//        sendMessage(messagePayloadJson, event.id, context);
    }

    private static ServiceBusMessagePayload createMessagePayload(EventSchema event) {
        return new ServiceBusMessagePayload(
                event.id,
                event.eventTime.toString(),
                new ServiceBusMessagePayload.ProducerInfo(
                        MetadataService.getProducerId(event.subject),
                        MetadataService.getProducerName(event.subject),
                        MetadataService.getProducerType(event.subject)
                ),
                new ServiceBusMessagePayload.FileInfo(
                        (String) event.data.get("fileName"),
                        (String) event.data.get("BlobUrl"),
                        (Long) event.data.get("FileSize"),
                        (String) event.data.get("FileType"),
                        (String) event.data.get("Checksum")
                ),
                new ServiceBusMessagePayload.MetadataInfo(
                        MetadataService.getMetadataId(event.id),
                        MetadataService.getPartitionKey(event.id),
                        MetadataService.getRowKey(event.id)
                ),
                new ServiceBusMessagePayload.ProcessingInfo(
                        "high", // Example priority
                        "2024-10-17T07:00:00Z" // Example deadline
                )
        );
    }

    public void close() {
        senderClient.close();
    }
}