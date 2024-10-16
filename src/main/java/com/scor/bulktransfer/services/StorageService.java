package com.scor.bulktransfer.services;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.microsoft.azure.functions.ExecutionContext;
import com.scor.bulktransfer.utils.Utils;

public class StorageService {

    private static final String STORAGE_CONNECTION_STRING = "";
    private static final String TABLE_NAME = "FileMetadata";
    private static final String PROCESSED_EVENTS_TABLE = "ProcessedEvents";
    private static final int EXPIRY_DAYS = 7;

    private final TableClient tableClient;
    private final TableClient processedEventsClient;

    private static StorageService INSTANCE = new StorageService();

    private StorageService() {
        this.tableClient = createTableClient(TABLE_NAME);
        this.processedEventsClient = createTableClient(PROCESSED_EVENTS_TABLE);
        createTableIfNotExists(tableClient);
        createTableIfNotExists(processedEventsClient);
    }

    // For testing
    public StorageService(TableClient tableClient, TableClient processedEventsClient) {
        this.tableClient = tableClient;
        this.processedEventsClient = processedEventsClient;
        createTableIfNotExists(tableClient);
        createTableIfNotExists(processedEventsClient);
    }

    public static StorageService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StorageService();
        }
        return INSTANCE;
    }

    private TableClient createTableClient(String tableName) {
        if (STORAGE_CONNECTION_STRING == null || STORAGE_CONNECTION_STRING.isEmpty()) {
            throw new IllegalStateException("Storage connection string is not set.");
        }
        return new TableClientBuilder().connectionString(STORAGE_CONNECTION_STRING).tableName(tableName).buildClient();
    }

    private void createTableIfNotExists(TableClient client) {
        try {
            client.createTable();
        } catch (TableServiceException e) {
            if (e.getResponse().getStatusCode() != 409) { // 409 Conflict == table already exists
                throw new IllegalStateException("Failed to create table: " + e.getMessage(), e);
            }
        }
    }

    public void logDataToTableStorage(String metadataJson, String eventId, ExecutionContext context) {
        String partitionKey = getInstance().generatePartitionKey(eventId);
        String rowKey = Utils.sanitizeForTableStorage(eventId);

        TableEntity entity = createTableEntity(partitionKey, rowKey, metadataJson);
        upsertTableEntity(tableClient, entity);

        markEventAsProcessed(eventId);

        context.getLogger().info(String.format("Data logged with PartitionKey: %s, RowKey: %s", partitionKey, rowKey));
    }

    public boolean isEventProcessed(String eventId) {
        try {
            TableEntity entity = processedEventsClient.getEntity("ProcessedEventsPartition", eventId);
            return entity != null;
        } catch (TableServiceException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
    }


    public void markEventAsProcessed(String eventId) {
        String partitionKey = "ProcessedEventsPartition";

        TableEntity processedEntity = new TableEntity(partitionKey, eventId).addProperty("ProcessedAt", Utils.getCurrentUtcTime());

        processedEventsClient.createEntity(processedEntity);
    }

    private TableEntity createTableEntity(String partitionKey, String rowKey, String metadataJson) {
        return new TableEntity(partitionKey, rowKey).addProperty("Metadata", metadataJson).addProperty("ExpiryTimestamp", Utils.getExpiryTimestamp(EXPIRY_DAYS));
    }

    private void upsertTableEntity(TableClient client, TableEntity entity) {
        client.upsertEntity(entity);
    }


/*
     In Azure Table Storage, if many requests target the same PartitionKey, it creates a hot partition,
     leading to throttled requests (HTTP 429), increased latency, and reduced overall throughput due to scalability limits
     the more random the partition key, the better.
*/

    public String generatePartitionKey(String eventId) {
//        Distributes across 1000 partitions
        return String.valueOf(eventId.hashCode() % 1000);
    }

}
