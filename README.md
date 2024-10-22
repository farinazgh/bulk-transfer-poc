# Bulk Transfer Draft

This repository contains Azure Functions and supporting services for handling messaging via Azure Service Bus. The solution includes:

- **Event Grid Trigger Function**: Processes events from Azure Event Grid when blobs are created in Azure Blob Storage.
- **Service Bus Messaging**: Sends messages to Service Bus Topics with filters for different consumers.
- **Cleanup Function**: Periodically cleans up expired entries from Azure Table Storage.
- **Consumers**: Java applications that consume messages from Service Bus Subscriptions based on filters.
- **Dead-Letter Handling**: Manages failed events by sending them to a dead-letter queue.

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

This project demonstrates how to integrate Azure Blob Storage, Event Grid, Azure Functions, Azure Service Bus, and Azure Table Storage and Azure message queue to build a messaging system with filtering capabilities. It includes:

- **Producers**: Upload files to Azure Blob Storage using azcopy, including metadata.
- **Event Grid**: Triggers an Azure Function when a blob is created.
- **Azure Function (EventGridMetadataLoggerFunction)**: Processes the event, extracts metadata, logs it to Azure Table Storage, and sends a message to an Azure Service Bus Topic.
- **Service Bus**: Routes messages to different subscriptions based on filters.
- **Consumers**: Java applications that consume messages from their respective subscriptions.
- **Cleanup Function**: Periodically deletes expired entries from Azure Table Storage.
- **Dead-Letter Handling**: Manages failed events by sending them to a dead-letter queue for further processing.

## Architecture Diagram

![Mermaid Diagram](https://github.com/farinazgh/bulk-transfer-poc/raw/main/mermaid-diagram.svg)


## Azure Services Used

- **Azure Blob Storage**: Stores files uploaded by producers.
- **Azure Event Grid**: Triggers events when blobs are created in Azure Blob Storage.
- **Azure Functions**: Hosts the EventGridMetadataLoggerFunction and CleanupFunction.
- **Azure Table Storage**: Stores metadata about processed files and events.
- **Azure Service Bus**: Manages messaging between the function and consumers, using topics and subscriptions with filters.
- **Azure Storage Queues**: Used for dead-lettering failed events.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven 3.6+
- Azure CLI installed and logged in
- Azure Subscription with permissions to create resources

### Clone the Repository

## Service Bus Setup

### Creating Topics and Subscriptions

TODO: Add the Azure CLI commands to create Service Bus topics and subscriptions.

### Applying Subscription Filters

TODO: Add the Azure CLI commands to apply filters to the subscriptions.

## Cleanup Function

The `CleanupFunction` is a time-triggered Azure Function that deletes expired entries from Azure Table Storage.

- **Schedule**: Once a day at midnight UTC.
- **Function Name**: `ExpiredEntriesCleanup`

## Dead-Letter Handling

The `EventGridMetadataLoggerFunction` includes dead-letter handling by sending failed events to an Azure Storage Queue (`deadletterqueue`). A separate function or process can monitor this queue to handle failed events.

## TODO: Azure CLI Commands

TODO: Add all the necessary Azure CLI commands for setting up the resources.

- Create Service Bus Namespace
- Create Service Bus Topic
- Create Subscriptions with Filters
- Set up Azure Storage resources
- Configure Event Grid subscriptions
