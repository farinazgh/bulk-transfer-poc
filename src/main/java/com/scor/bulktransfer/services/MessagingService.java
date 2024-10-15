package com.scor.bulktransfer.services;


import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.microsoft.azure.functions.ExecutionContext;

/**
 * sends the message to azure servicebus
 */

public class MessagingService {
    private static final String SERVICE_BUS_CONNECTION_STRING = System.getenv("SERVICE_BUS_CONNECTION_STRING");
    private static final String SERVICE_BUS_QUEUE_NAME = "bulktransferneu";

    private final ServiceBusSenderClient senderClient;

    // Singleton Instance
    private static final MessagingService INSTANCE = new MessagingService();

    private MessagingService() {
        if (SERVICE_BUS_CONNECTION_STRING == null || SERVICE_BUS_CONNECTION_STRING.isEmpty()) {
            throw new IllegalStateException("Service Bus connection string is not set.");
        }

        this.senderClient = new ServiceBusClientBuilder()
                .connectionString(SERVICE_BUS_CONNECTION_STRING)
                .sender()
                .queueName(SERVICE_BUS_QUEUE_NAME)
                .buildClient();
    }

    public static MessagingService getInstance() {
        return INSTANCE;
    }

    public void sendMessage(String messageContent, ExecutionContext context) {
        try {
            ServiceBusMessage message = new ServiceBusMessage(messageContent);
            senderClient.sendMessage(message);
            context.getLogger().info("Message sent to Service Bus queue.");
        } catch (ServiceBusException e) {
            context.getLogger().severe("Service Bus error: " + e.getMessage());
            throw new RuntimeException("Failed to send message to Service Bus", e);
        }
    }


    public void close() {
        senderClient.close();
    }
}