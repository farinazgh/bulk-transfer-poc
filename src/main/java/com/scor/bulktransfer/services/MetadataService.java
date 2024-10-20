package com.scor.bulktransfer.services;


import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.scor.bulktransfer.models.EventSchema;
import com.scor.bulktransfer.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * extracts metadata from EventSchema.
 */
public class MetadataService {

    private static final int EXPIRY_DAYS = 7;

    private static final Map<String, MetadataAttributes> metadataStore = new HashMap<>();
    private static Map<String, Object> eventData = new HashMap<>();

    static public Map<String, Object> createMetadata(EventSchema event) {

        String url = Utils.extractValidUrl(event.data.get("url"));
        String fileName = Utils.extractFileNameFromUrl(url);

        eventData.put("Subject", Utils.nullSafe(event.subject));
        eventData.put("Source", Utils.nullSafe(event.source));
        eventData.put("Time", Utils.nullSafe(event.eventTime));
        eventData.put("Topic", Utils.nullSafe(event.topic));
        eventData.put("Id", Utils.nullSafe(event.id));
        eventData.put("Api", Utils.nullSafe(event.data.get("api")));
        eventData.put("ClientRequestId", Utils.nullSafe(event.data.get("clientRequestId")));
        eventData.put("RequestId", Utils.nullSafe(event.data.get("requestId")));
        eventData.put("ETag", Utils.nullSafe(event.data.get("eTag")));
        eventData.put("ContentType", Utils.nullSafe(event.data.get("contentType")));
        eventData.put("ContentLength", Utils.nullSafe(event.data.get("contentLength")));
        eventData.put("BlobType", Utils.nullSafe(event.data.get("blobType")));
        eventData.put("Url", url);
        eventData.put("FileName", fileName);
        eventData.put("ContainerName", Utils.extractContainerName(url));
        eventData.put("Sequencer", Utils.nullSafe(event.data.get("sequencer")));
        eventData.put("EventTime", Utils.nullSafe(event.eventTime));
        eventData.put("ProcessingTime", Utils.getCurrentUtcTime());
        eventData.put("ExpiryTimestamp", Utils.getExpiryTimestamp(EXPIRY_DAYS));


        createBlobMetadata();

        return eventData;
    }


    private static void createBlobMetadata() {
        String connectionString = "DefaultEndpointsProtocol=https;AccountName=bulktransferneu;AccountKey=74MNLQD6BITNwyD59iZdDkC9tJh9JtQk852G+GFhtqUbMiJfd3ozj2iYCekW8oJ/HtbOJnbV/p8l+AStlXttig==;EndpointSuffix=core.windows.net";
        String containerName = String.valueOf(eventData.get("ContainerName"));
        String blobName = String.valueOf(eventData.get("FileName"));
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        BlobProperties properties = blobClient.getProperties();
        Map<String, String> metadata = properties.getMetadata();
        eventData.putAll(metadata);
    }


    static public String getMetadataId(String eventId) {
        MetadataAttributes attributes = metadataStore.get(eventId);
        return attributes != null ? attributes.getMetadataId() : null;
    }


    static public String getPartitionKey(String eventId) {
        MetadataAttributes attributes = metadataStore.get(eventId);
        return attributes != null ? attributes.getPartitionKey() : null;
    }


    static public String getRowKey(String eventId) {
        MetadataAttributes attributes = metadataStore.get(eventId);
        return attributes != null ? attributes.getRowKey() : null;
    }

    static public String getProducerId(String subject) {
        // Implement logic to extract Producer ID from the subject or other relevant fields
        return "producer-" + subject.split("/")[4].toLowerCase(); // Example extraction based on container name
    }


    static public String getProducerName(String subject) {
        // Implement logic to extract Producer Name from the subject or other relevant fields
        String[] parts = subject.split("/");
        return parts.length >= 5 ? capitalizeFirstLetter(parts[4]) : "Unknown";
    }


    static public String getProducerType(String subject) {
        // Implement logic to determine Producer Type
        // Example: Return a fixed type or extract from subject
        return "AKSCluster"; // Example value
    }


    static public boolean isEventProcessed(String eventId) {
        return metadataStore.containsKey(eventId);
    }


    private static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


    private static class MetadataAttributes {
        private final String metadataId;
        private final String partitionKey;
        private final String rowKey;
        private final String producerId;
        private final String producerName;
        private final String producerType;

        public MetadataAttributes(String metadataId, String partitionKey, String rowKey,
                                  String producerId, String producerName, String producerType) {
            this.metadataId = metadataId;
            this.partitionKey = partitionKey;
            this.rowKey = rowKey;
            this.producerId = producerId;
            this.producerName = producerName;
            this.producerType = producerType;
        }

        public String getMetadataId() {
            return metadataId;
        }

        public String getPartitionKey() {
            return partitionKey;
        }

        public String getRowKey() {
            return rowKey;
        }

        public String getProducerId() {
            return producerId;
        }

        public String getProducerName() {
            return producerName;
        }

        public String getProducerType() {
            return producerType;
        }
    }
}