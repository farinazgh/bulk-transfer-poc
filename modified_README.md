
# Bulk Transfer Draft

This repository contains Azure Functions and supporting services for handling messaging via Azure Service Bus. The solution includes:

- **Event Grid Trigger Function**: [EventGridMetadataLoggerFunction](src/main/java/com/scor/bulktransfer/functions/EventGridMetadataLoggerFunction.java) processes events from Azure Event Grid when blobs are created in Azure Blob Storage.
- **Service Bus Messaging**: [MessagingService](src/main/java/com/scor/bulktransfer/services/MessagingService.java) sends messages to Service Bus Topics with filters for different consumers.
- **Cleanup Function**: [CleanupFunction](src/main/java/com/scor/bulktransfer/functions/CleanupFunction.java) periodically cleans up expired entries from Azure Table Storage.
- **Consumers**: Java applications ([ServiceBusTestConsumer](src/main/java/com/scor/bulktransfer/services/ServiceBusTestConsumer.java)) that consume messages from Service Bus Subscriptions based on filters.
- **Dead-Letter Handling**: Manages failed events by sending them to a dead-letter queue ([EventGridMetadataLoggerFunction](src/main/java/com/scor/bulktransfer/functions/EventGridMetadataLoggerFunction.java)).

## Table of Contents

- [Overview](#overview)
- [Architecture Diagram](#architecture-diagram)
- [Azure Services Used](#azure-services-used)
- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Clone the Repository](#clone-the-repository)
- [Setting Up Environment Variables](#setting-up-environment-variables)
- [Building and Deploying the Azure Functions](#building-and-deploying-the-azure-functions)
- [Service Bus Setup](#service-bus-setup)
- [Creating Topics and Subscriptions](#creating-topics-and-subscriptions)
- [Applying Subscription Filters](#applying-subscription-filters)
- [Running the Test Applications](#running-the-test-applications)
- [Cleanup Function](#cleanup-function)
- [Dead-Letter Handling](#dead-letter-handling)
- [TODO: Azure CLI Commands](#todo-azure-cli-commands)
- [Contributing](#contributing)
- [License](#license)

## Overview

This project demonstrates how to integrate Azure Blob Storage, Event Grid, Azure Functions, Azure Service Bus, and Azure Table Storage to build a messaging system with filtering capabilities. It includes:

- **Producers**: Upload files to Azure Blob Storage using `azcopy`, including metadata.
- **Event Grid**: Triggers an Azure Function when a blob is created.
- **Azure Function (EventGridMetadataLoggerFunction)**: [Processes](src/main/java/com/scor/bulktransfer/functions/EventGridMetadataLoggerFunction.java) the blob creation event, logs metadata, and sends a message to Service Bus Topics.
- **Azure Table Storage**: [Logs](src/main/java/com/scor/bulktransfer/services/StorageService.java) metadata and event processing details.
- **Service Bus**: [Manages](src/main/java/com/scor/bulktransfer/services/MessagingService.java) the routing of messages from the Azure Function to consumers via subscriptions.
- **Consumers**: [Consume](src/main/java/com/scor/bulktransfer/services/ServiceBusTestConsumer.java) messages from Service Bus Topics based on specific filters.
- **Dead-Letter Handling**: Handles failed events by sending them to a dead-letter queue ([EventGridMetadataLoggerFunction](src/main/java/com/scor/bulktransfer/functions/EventGridMetadataLoggerFunction.java)) for further processing.

## Architecture Diagram

![Mermaid Diagram](mermaid-diagram-2024-10-22-194843.svg)

## Azure Services Used

- **Azure Blob Storage**: Stores files uploaded by producers.
- **Azure Event Grid**: Triggers events when blobs are created in Azure Blob Storage.
- **Azure Functions**: Hosts the [EventGridMetadataLoggerFunction](src/main/java/com/scor/bulktransfer/functions/EventGridMetadataLoggerFunction.java) and [CleanupFunction](src/main/java/com/scor/bulktransfer/functions/CleanupFunction.java).
- **Azure Table Storage**: [Stores metadata](src/main/java/com/scor/bulktransfer/services/StorageService.java) about processed files and events.
- **Azure Service Bus**: [Manages messaging](src/main/java/com/scor/bulktransfer/services/MessagingService.java) between the function and consumers, using topics and subscriptions with filters.
- **Azure Storage Queues**: Used for [dead-lettering](src/main/java/com/scor/bulktransfer/functions/EventGridMetadataLoggerFunction.java) failed events.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven 3.6+
- Azure CLI installed and logged in
- Azure Subscription with permissions to create resources

### Clone the Repository

```bash
git clone https://github.com/yourusername/yourrepository.git
cd yourrepository
```

### Setting Up Environment Variables

Set the necessary environment variables:

```bash
export STORAGE_CONNECTION_STRING="<your-storage-connection-string>"
export SERVICE_BUS_CONNECTION_STRING="<your-service-bus-connection-string>"
export TOPIC_NAME="<your-topic-name>"
export TABLE_NAME="FileMetadata"
export DEAD_LETTER_QUEUE_NAME="deadletterqueue"
```

## Building and Deploying the Azure Functions

Use Maven to build and deploy the Azure Functions.

```bash
mvn clean package
mvn azure-functions:deploy
```

## Service Bus Setup

### Creating Topics and Subscriptions

**TODO**: Add the Azure CLI commands to create Service Bus topics and subscriptions.

### Applying Subscription Filters

**TODO**: Add the Azure CLI commands to apply filters to the subscriptions.

## Running the Test Applications

### Test Sender

The `ServiceBusTestSender` sends test messages to the Service Bus topic.

```bash
java -cp target/yourjarfile.jar com.scor.bulktransfer.services.ServiceBusTestSender
```

### Test Consumers

Run the consumer for each subscription:

```bash
java -cp target/yourjarfile.jar com.scor/bulktransfer/services/ServiceBusTestConsumer     "$SERVICE_BUS_CONNECTION_STRING" "$TOPIC_NAME" "OmegaSubscription"
```

## Cleanup Function

The [CleanupFunction](src/main/java/com/scor/bulktransfer/functions/CleanupFunction.java) is a time-triggered Azure Function that deletes expired entries from Azure Table Storage.

- **Schedule**: Once a day at midnight UTC.
- **Function Name**: `ExpiredEntriesCleanup`

## Dead-Letter Handling

The [EventGridMetadataLoggerFunction](src/main/java/com/scor/bulktransfer/functions/EventGridMetadataLoggerFunction.java) includes dead-letter handling by sending failed events to an Azure Storage Queue (`deadletterqueue`). A separate function or process can monitor this queue to handle failed events.

## TODO: Azure CLI Commands

**TODO**: Add all the necessary Azure CLI commands for setting up the resources.

- Create Service Bus Namespace
- Create Service Bus Topic
- Create Subscriptions with Filters
- Set up Azure Storage resources
- Configure Event Grid subscriptions

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

This project is licensed under the MIT License.
