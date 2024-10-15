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
    //    private static final String SERVICE_BUS_CONNECTION_STRING = System.getenv("SERVICE_BUS_CONNECTION_STRING");
    private static final String SERVICE_BUS_CONNECTION_STRING = "Endpoint=sb://bulktransferneu.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=o9hc7VoDrXTtn2W4g4Y3PCoHmFTQO179k+ASbOu9Jrg=";

    private static final String SERVICE_BUS_QUEUE_NAME = "bulktransferneu";

    private final ServiceBusSenderClient senderClient;

    public MessagingService() {
        senderClient = new ServiceBusClientBuilder()
                .connectionString(SERVICE_BUS_CONNECTION_STRING)
                .sender()
                .queueName(SERVICE_BUS_QUEUE_NAME)
                .buildClient();
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
    //todo to close or not to close the client?
}
