package luna;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Function triggered by Event Grid.
 */
public class Function {
    private static final String STORAGE_CONNECTION_STRING = "";

    private static final String TABLE_NAME = "FileMetadata";

    @FunctionName("EventGridListener")
    public void run(
            @EventGridTrigger(name = "event") EventSchema event,
            final ExecutionContext context) {

        Map<String, Object> metadata = createMetadata(event);
        String metadataJson = convertToJson(metadata);
        logDataToTableStorage(metadataJson, event.id, context);
    }

    private static Map<String, Object> createMetadata(EventSchema event) {
        Map<String, Object> metadata = new HashMap<>();

        String url = String.valueOf(event.data.get("url"));
        String fileName = extractFileNameFromUrl(url);

        metadata.put("Subject", event.subject);
        metadata.put("Id", event.id);
        metadata.put("Api", event.data.get("api"));
        metadata.put("ClientRequestId", event.data.get("clientRequestId"));
        metadata.put("RequestId", event.data.get("requestId"));
        metadata.put("ETag", event.data.get("eTag"));
        metadata.put("ContentType", event.data.get("contentType"));
        metadata.put("ContentLength", event.data.get("contentLength"));
        metadata.put("BlobType", event.data.get("blobType"));
        metadata.put("Url", url);
        metadata.put("FileName", fileName);
        metadata.put("Sequencer", event.data.get("sequencer"));
        metadata.put("EventTime", event.eventTime);
        metadata.put("ProcessingTime", getCurrentUtcTime());
        metadata.put("ExpiryTimestamp", getExpiryTimestamp());

        return metadata;
    }


    private static String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private static String getCurrentUtcTime() {
        return OffsetDateTime.now(ZoneOffset.UTC).toString();
    }

    private static String getExpiryTimestamp() {
        return OffsetDateTime.now(ZoneOffset.UTC).plusDays(7).toString();
    }

    private static String convertToJson(Map<String, Object> metadata) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting metadata to JSON", e);
        }
    }

    private void logDataToTableStorage(String metadataJson, String eventId, ExecutionContext context) {
        TableClient tableClient = getTableClient();
        createTableIfNotExists(tableClient, context);

        String partitionKey = getPartitionKey();
        String rowKey = sanitizeForTableStorage(eventId);

        TableEntity entity = createTableEntity(partitionKey, rowKey, metadataJson);
        upsertTableEntity(tableClient, entity, context);

        context.getLogger().info("File information logged into Azure Table Storage with PartitionKey: " + partitionKey + ", RowKey: " + rowKey);
    }

    private TableClient getTableClient() {
        return new TableClientBuilder()
                .connectionString(STORAGE_CONNECTION_STRING)
                .tableName(TABLE_NAME)
                .buildClient();
    }

    private void createTableIfNotExists(TableClient tableClient, ExecutionContext context) {
        try {
            tableClient.createTable();
            context.getLogger().info("Table created: " + TABLE_NAME);
        } catch (TableServiceException e) {
            if (e.getResponse().getStatusCode() != 409) { // 409 Conflict -> table already exists
                throw new RuntimeException("Failed to create table: " + e.getMessage(), e);
            }
        }
    }

    private String getPartitionKey() {
        return OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private TableEntity createTableEntity(String partitionKey, String rowKey, String metadataJson) {
        return new TableEntity(partitionKey, rowKey)
                .addProperty("Metadata", metadataJson)
                .addProperty("ExpiryTimestamp", OffsetDateTime.now(ZoneOffset.UTC).plusDays(7).toString());
    }

    private void upsertTableEntity(TableClient tableClient, TableEntity entity, ExecutionContext context) {
        tableClient.upsertEntity(entity);
    }


    private String sanitizeForTableStorage(String key) {
        // not allowed characters in PartitionKey and RowKey
        return key.replace("/", "")
                .replace("\\", "")
                .replace("#", "")
                .replace("?", "");
    }
}
