package com.scor.bulktransfer.services;

import com.scor.bulktransfer.models.EventSchema;
import com.scor.bulktransfer.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * extracts metadata from EventSchema.
 */
public class MetadataService {

    private static final int EXPIRY_DAYS = 7;

    // Singleton Instance
    private static final MetadataService INSTANCE = new MetadataService();

    private MetadataService() {
        // Private constructor to prevent instantiation
    }

    public static MetadataService getInstance() {
        return INSTANCE;
    }

    public Map<String, Object> createMetadata(EventSchema event) {
        Map<String, Object> metadata = new HashMap<>();

        // Extract and validate required fields
        String url = Utils.extractValidUrl(event.data.get("url"));
        String fileName = Utils.extractFileNameFromUrl(url);

        metadata.put("Subject", Utils.nullSafe(event.subject));
        metadata.put("Id", Utils.nullSafe(event.id));
        metadata.put("Api", Utils.nullSafe(event.data.get("api")));
        metadata.put("ClientRequestId", Utils.nullSafe(event.data.get("clientRequestId")));
        metadata.put("RequestId", Utils.nullSafe(event.data.get("requestId")));
        metadata.put("ETag", Utils.nullSafe(event.data.get("eTag")));
        metadata.put("ContentType", Utils.nullSafe(event.data.get("contentType")));
        metadata.put("ContentLength", Utils.nullSafe(event.data.get("contentLength")));
        metadata.put("BlobType", Utils.nullSafe(event.data.get("blobType")));
        metadata.put("Url", url);
        metadata.put("FileName", fileName);
        metadata.put("Sequencer", Utils.nullSafe(event.data.get("sequencer")));
        metadata.put("EventTime", Utils.nullSafe(event.eventTime));
        metadata.put("ProcessingTime", Utils.getCurrentUtcTime());
        metadata.put("ExpiryTimestamp", Utils.getExpiryTimestamp(EXPIRY_DAYS));

        return metadata;
    }
}
