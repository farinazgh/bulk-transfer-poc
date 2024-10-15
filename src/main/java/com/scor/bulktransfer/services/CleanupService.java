package com.scor.bulktransfer.services;


import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.microsoft.azure.functions.ExecutionContext;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * cleans up expired entries in Azure Table Storage
 */
public class CleanupService {

    private static final String STORAGE_CONNECTION_STRING = System.getenv("STORAGE_CONNECTION_STRING");
    private static final String TABLE_NAME = "FileMetadata";

    private final TableClient tableClient;

    public CleanupService() {
        this.tableClient = createTableClient();
    }

    private TableClient createTableClient() {
        if (STORAGE_CONNECTION_STRING == null || STORAGE_CONNECTION_STRING.isEmpty()) {
            throw new IllegalStateException("Storage connection string is not set.");
        }
        return new TableClientBuilder()
                .connectionString(STORAGE_CONNECTION_STRING)
                .tableName(TABLE_NAME)
                .buildClient();
    }

    public int deleteExpiredEntries(ExecutionContext context) {
        String expiryTime = OffsetDateTime.now(ZoneOffset.UTC).toString();
        String filter = "ExpiryTimestamp lt '" + expiryTime + "'";

        PagedIterable<TableEntity> expiredEntities = tableClient.listEntities(
                new ListEntitiesOptions().setFilter(filter), null, Context.NONE);

        int deletedCount = 0;

        for (TableEntity entity : expiredEntities) {
            try {
                tableClient.deleteEntity(entity.getPartitionKey(), entity.getRowKey());
                deletedCount++;
            } catch (Exception e) {
                context.getLogger().warning("Failed to delete entity with PartitionKey: "
                        + entity.getPartitionKey() + ", RowKey: " + entity.getRowKey() + ". Error: " + e.getMessage());
            }
        }

        return deletedCount;
    }
}
