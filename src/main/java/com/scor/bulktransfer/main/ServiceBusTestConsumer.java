package com.scor.bulktransfer.main;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scor.bulktransfer.models.MessagePayload;

import java.util.Scanner;

//todo test cross delivery
public class ServiceBusTestConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String subscriptionName;

    public ServiceBusTestConsumer(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public static void main(String[] args) {
        String SERVICE_BUS_CONNECTION_STRING = System.getenv("SERVICE_BUS_CONNECTION_STRING");

        String TOPIC_NAME = "bulktransfer-blobevents-topic";

        String subscriptionName = "AlphaSubscription";
        ServiceBusTestConsumer consumer = new ServiceBusTestConsumer(subscriptionName);

        consumer.receiveMessages(SERVICE_BUS_CONNECTION_STRING, TOPIC_NAME);
    }

    public void receiveMessages(String connectionString, String topicName) {
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .processor()
                .topicName(topicName)
                .subscriptionName(subscriptionName)
                .processMessage(this::processMessage)
                .processError(context -> System.out.printf("Error occurred: %s%n", context.getException()))
                .buildProcessorClient();

        processorClient.start();
        System.out.println("Receiving messages for subscription: " + subscriptionName + ". Press ENTER to stop.");
        new Scanner(System.in).nextLine();

        processorClient.close();
    }

    private void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();

        String body = message.getBody().toString();
        String producerName = (String) message.getApplicationProperties().get("producerName");
        String producerType = (String) message.getApplicationProperties().get("producerType");
        String producerId = (String) message.getApplicationProperties().get("producerId");

        MessagePayload payload;
        try {
            payload = objectMapper.readValue(body, MessagePayload.class);
        } catch (Exception e) {
            System.err.println("Failed to deserialize message body: " + e.getMessage());
            context.abandon();
            return;
        }

        System.out.printf("Received message on subscription '%s': %s%n", subscriptionName, body);
        System.out.printf("ProducerName: %s, ProducerID: %s, BlobURL: %s%n", producerName, producerId, payload.getBlobURL());
        context.complete();
    }
}
