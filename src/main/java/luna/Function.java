package luna;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
        context.getLogger().info("Event content: ");
        context.getLogger().info("Subject: " + event.subject);
        context.getLogger().info("Time: " + event.eventTime); // automatically converted to Date by the runtime
        context.getLogger().info("Id: " + event.id);
        context.getLogger().info("Data: " + event.data);
        context.getLogger().info("Data: " + event.data.keySet());
        context.getLogger().info("Data: " + event.data.values());

        String url = String.valueOf(event.data.get("url"));
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        Map<String, Object> metadata = new HashMap<>();
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
//        metadata.put("BatchId", batchId);
        metadata.put("EventTime", event.eventTime);
        metadata.put("ProcessingTime", OffsetDateTime.now(ZoneOffset.UTC).toString());
        metadata.put("ExpiryTimestamp", OffsetDateTime.now(ZoneOffset.UTC).plusDays(7).toString());
        context.getLogger().info(">>>>>>>>>>>> metadata <<<<<<<<<<<<<<<<<<<");
        context.getLogger().info(metadata.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        context.getLogger().info("******************* json value ***************************");
        context.getLogger().info(metadataJson);
        context.getLogger().info("**************************** after ***************************");

        logDataToTableStorage(metadataJson, event.id, context);
    }

    private void logDataToTableStorage(String metadataJson, String eventId, ExecutionContext context) {
        TableClient tableClient = new TableClientBuilder()
                .connectionString(STORAGE_CONNECTION_STRING)
                .tableName(TABLE_NAME)
                .buildClient();

        // Create table if not exist
        try {
            tableClient.createTable();
            context.getLogger().info("Table created: " + TABLE_NAME);
        } catch (TableServiceException e) {
            if (e.getResponse().getStatusCode() != 409) { // 409 Conflict -> table already exists
                throw new RuntimeException("Failed to create table: " + e.getMessage(), e);
            }
        }

        // Partition by date for better scalability
        String partitionKey = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String rowKey = sanitizeForTableStorage(eventId);

        TableEntity entity = new TableEntity(partitionKey, rowKey)
                .addProperty("Metadata", metadataJson)
                .addProperty("ExpiryTimestamp", OffsetDateTime.now(ZoneOffset.UTC).plusDays(7).toString());

        tableClient.upsertEntity(entity);

        context.getLogger().info("File information logged into Azure Table Storage with PartitionKey: " + partitionKey + ", RowKey: " + rowKey);
    }

    private String sanitizeForTableStorage(String key) {
        // Disallowed characters in PartitionKey and RowKey are: '/', '\\', '#', '?'
        return key.replace("/", "")
                .replace("\\", "")
                .replace("#", "")
                .replace("?", "");
    }
}
