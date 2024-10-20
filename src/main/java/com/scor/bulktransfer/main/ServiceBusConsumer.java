package com.scor.bulktransfer.main;


//import com.azure.identity.DefaultAzureCredential;
//import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.*;

import java.util.concurrent.TimeUnit;

public class ServiceBusConsumer {
    private final ServiceBusProcessorClient processorClient;
    String serviceBusConnectionString;
    static String serviceBusTopicName;
    static String serviceBusSubscriptionName;

    public ServiceBusConsumer() {
        serviceBusConnectionString = System.getenv("SERVICE_BUS_CONNECTION_STRING");
        serviceBusTopicName = System.getenv("SERVICE_BUS_TOPIC_NAME");
        serviceBusSubscriptionName = System.getenv("SERVICE_BUS_SUBSCRIPTION_NAME");

        if (serviceBusConnectionString == null || serviceBusConnectionString.isEmpty()) {
            throw new IllegalStateException("Service Bus connection string is not set.");
        }

        if (serviceBusTopicName == null || serviceBusTopicName.isEmpty()) {
            throw new IllegalStateException("Service Bus topic name is not set.");
        }

        if (serviceBusSubscriptionName == null || serviceBusSubscriptionName.isEmpty()) {
            throw new IllegalStateException("Service Bus subscription name is not set.");
        }


        this.processorClient = new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .processor()
                .topicName(serviceBusTopicName)
                .subscriptionName(serviceBusSubscriptionName)
                .processMessage(this::processMessage)
                .processError(this::processError)
                .prefetchCount(20)
                .maxConcurrentCalls(5)
                .buildProcessorClient();
    }

    public void start() {
        processorClient.start();
    }

    /**
     * Stops the Service Bus Processor.
     */
    public void stop() {
        processorClient.stop();
        processorClient.close();
        System.out.println("Service Bus Consumer stopped.");
    }


    // handles received messages
/*
    void receiveMessages() throws InterruptedException {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();

        // Create an instance of the processor through the ServiceBusClientBuilder
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .fullyQualifiedNamespace("NAMESPACENAME.servicebus.windows.net")
                .credential(credential)
                .processor()
                .topicName(serviceBusTopicName)
                .subscriptionName(serviceBusSubscriptionName)
                .processMessage(this::processMessage)
                .processError(this::processError)
                .buildProcessorClient();

        System.out.println("Starting the processor");
        processorClient.start();

        TimeUnit.SECONDS.sleep(10);
        System.out.println("Stopping and closing the processor");
        processorClient.close();
    }
*/

    private void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n", message.getMessageId(),
                message.getSequenceNumber(), message.getBody());
    }

    private void processError(ServiceBusErrorContext context) {
        System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException)) {
            System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();

        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
                || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
                || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            System.out.printf("An unrecoverable error occurred. Stopping processing with reason %s: %s%n",
                    reason, exception.getMessage());
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            System.out.printf("Message lock lost for message: %s%n", context.getException());
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            try {
                // Choosing an arbitrary amount of time to wait until trying again.
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.err.println("Unable to sleep for period of time");
            }
        } else {
            System.out.printf("Error source %s, reason %s, message: %s%n", context.getErrorSource(),
                    reason, context.getException());
        }
    }


    // Example main method to run the consumer as a standalone application
    public static void main(String[] args) throws InterruptedException {
        // Create a simple ExecutionContext implementation for standalone application


        ServiceBusConsumer consumer = new ServiceBusConsumer();
        consumer.start();

        // Keep the application running
        System.out.println("Press Enter to exit...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Handle exception
        }

        consumer.stop();
    }
}

