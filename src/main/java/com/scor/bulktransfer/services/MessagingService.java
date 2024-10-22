package com.scor.bulktransfer.services;

import com.azure.messaging.servicebus.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.scor.bulktransfer.models.MessagePayload;

import java.util.Map;

public class MessagingService {
    private static final String SERVICE_BUS_CONNECTION_STRING = "";
    private static final String TOPIC_NAME = "";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendMessageToServiceBus(Map<String, Object> metadata, ExecutionContext context) {

        try (ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(SERVICE_BUS_CONNECTION_STRING)
                .sender()
                .topicName(TOPIC_NAME)
                .buildClient()) {

            MessagePayload payload = createMessagePayload(metadata);
            String messageBody = objectMapper.writeValueAsString(payload);
            ServiceBusMessage message = new ServiceBusMessage(messageBody);
            message.getApplicationProperties().put("producerName", metadata.getOrDefault("producerName", ""));
            message.getApplicationProperties().put("producerId", metadata.getOrDefault("producerId", ""));
            message.getApplicationProperties().put("producerType", metadata.getOrDefault("producerType", ""));
            senderClient.sendMessage(message);
            context.getLogger().info("Message sent to Service Bus topic.");

        } catch (JsonProcessingException e) {
            context.getLogger().severe("Failed to serialize message payload: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            context.getLogger().severe("Failed to send message to Service Bus: " + e.getMessage());
            throw e;
        }
    }

    private static MessagePayload createMessagePayload(Map<String, Object> metadata) {

        String blobUrl = (String) metadata.getOrDefault("Url", "");
        String checksum = (String) metadata.getOrDefault("Checksum", "");
        String fileSize = String.valueOf(metadata.getOrDefault("ContentLength", ""));
        String blobType = (String) metadata.getOrDefault("BlobType", "");

        return new MessagePayload(blobUrl, checksum, fileSize, blobType);
    }
}
