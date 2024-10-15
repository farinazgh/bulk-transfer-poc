package com.scor.bulktransfer.services;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.microsoft.azure.functions.ExecutionContext;
import com.scor.bulktransfer.utils.Utils;

/**
 * interacts with Azure Table Storage.
 */
public class StorageService {

    private static final String STORAGE_CONNECTION_STRING = System.getenv("STORAGE_CONNECTION_STRING");

    private static final String TABLE_NAME = "FileMetadata";
    private static final int EXPIRY_DAYS = 7;

    private final TableClient tableClient;

    private static final StorageService INSTANCE = new StorageService();

    private StorageService() {
        this.tableClient = createTableClient(TABLE_NAME);
        createTableIfNotExists(tableClient);
    }

    public static StorageService getInstance() {
        return INSTANCE;
    }

    private TableClient createTableClient(String tableName) {
        if (STORAGE_CONNECTION_STRING == null || STORAGE_CONNECTION_STRING.isEmpty()) {
            throw new IllegalStateException("Storage connection string is not set.");
        }
        return new TableClientBuilder()
                .connectionString(STORAGE_CONNECTION_STRING)
                .tableName(tableName)
                .buildClient();
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
        String partitionKey = Utils.getCurrentDate();
        String rowKey = Utils.sanitizeForTableStorage(eventId);

        TableEntity entity = createTableEntity(partitionKey, rowKey, metadataJson);
        upsertTableEntity(entity);

        context.getLogger().info(String.format("Data logged with PartitionKey: %s, RowKey: %s", partitionKey, rowKey));
    }

    private TableEntity createTableEntity(String partitionKey, String rowKey, String metadataJson) {
        return new TableEntity(partitionKey, rowKey)
                .addProperty("Metadata", metadataJson)
                .addProperty("ExpiryTimestamp", Utils.getExpiryTimestamp(EXPIRY_DAYS));
    }

    private void upsertTableEntity(TableEntity entity) {
        tableClient.upsertEntity(entity);
    }
}
