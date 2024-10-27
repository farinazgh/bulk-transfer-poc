package com.scor.bulktransfer.services;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.scor.bulktransfer.models.EventSchema;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling dead-lettering of failed events.
 */
public class DeadLetterQueueService {

    private static final String QUEUE_CONNECTION_STRING = System.getenv("QUEUE_CONNECTION_STRING");
    private static final String QUEUE_NAME = "deadletter"; // Replace with your queue name

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final QueueClient queueClient;

    static {
        if (QUEUE_CONNECTION_STRING == null || QUEUE_CONNECTION_STRING.isEmpty()) {
            throw new IllegalStateException("Queue connection string is not set. Please set the QUEUE_CONNECTION_STRING environment variable.");
        }

        try {
            queueClient = new QueueClientBuilder()
                    .connectionString(QUEUE_CONNECTION_STRING)
                    .queueName(QUEUE_NAME)
                    .buildClient();

            queueClient.create();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing DeadLetterQueueService: " + e.getMessage(), e);
        }
    }


    public static void sendToDeadLetter(EventSchema event, String errorMessage, ExecutionContext context) {
        try {
            Map<String, Object> deadLetterPayload = new HashMap<>();
            deadLetterPayload.put("errorMessage", errorMessage);
            deadLetterPayload.put("failedEvent", event);

            String deadLetterMessageJson = objectMapper.writeValueAsString(deadLetterPayload);

            String encodedMessage = Base64.getEncoder().encodeToString(deadLetterMessageJson.getBytes(StandardCharsets.UTF_8));

            SendMessageResult sendMessageResult = queueClient.sendMessage(encodedMessage);
            context.getLogger().info("Event sent to dead-letter queue. Message ID: " + sendMessageResult.getMessageId());
        } catch (Exception ex) {
            context.getLogger().severe("Failed to send event to dead-letter queue: " + ex.getMessage());
            context.getLogger().severe(getStackTraceAsString(ex));
            // dont rethrow, poisonous blocking
        }
    }

    public static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
