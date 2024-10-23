package com.scor.bulktransfer.main;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scor.bulktransfer.models.MessagePayload;

import java.util.HashMap;
import java.util.Map;

public class ServiceBusTestSender {
    private static final String SERVICE_BUS_CONNECTION_STRING = System.getenv("SERVICE_BUS_CONNECTION_STRING");
    private static final String TOPIC_NAME = "";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        sendTestMessage("omega");
        sendTestMessage("alpha");
        sendTestMessage("gamma");
    }

    private static void sendTestMessage(String producerName) {
        try (ServiceBusSenderClient senderClient = new ServiceBusClientBuilder().connectionString(SERVICE_BUS_CONNECTION_STRING).sender().topicName(TOPIC_NAME).buildClient()) {

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("Url", "https://bulktransferneu.blob.core.windows.net/vega/" + producerName + ".txt");
            metadata.put("Checksum", "sampleChecksumValue");
            metadata.put("ContentLength", "2.0");
            metadata.put("BlobType", "BlockBlob");
            metadata.put("producerName", producerName);
            metadata.put("producerId", "98761");
            metadata.put("producerType", "aks-cluster");

            MessagePayload payload = createMessagePayload(metadata);

            String messageBody = objectMapper.writeValueAsString(payload);

            ServiceBusMessage message = new ServiceBusMessage(messageBody);

            message.getApplicationProperties().put("producerName", producerName);
            message.getApplicationProperties().put("producerId", metadata.getOrDefault("producerId", ""));
            message.getApplicationProperties().put("producerType", metadata.getOrDefault("producerType", ""));

            senderClient.sendMessage(message);

            System.out.println("Message sent to Service Bus topic for producer: " + producerName);

        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize message payload: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("Failed to send message to Service Bus: " + e.getMessage());
            throw e;
        }
    }

    private static MessagePayload createMessagePayload(Map<String, Object> metadata) {
        System.out.println("test");
        String blobUrl = (String) metadata.getOrDefault("Url", "");
        String checksum = (String) metadata.getOrDefault("Checksum", "");
        String fileSize = String.valueOf(metadata.getOrDefault("ContentLength", ""));
        String blobType = (String) metadata.getOrDefault("BlobType", "");

        return new MessagePayload(blobUrl, checksum, fileSize, blobType);
    }
}
