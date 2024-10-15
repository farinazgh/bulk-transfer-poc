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

    public Map<String, Object> createMetadata(EventSchema event) {
        Map<String, Object> metadata = new HashMap<>();

        String url = String.valueOf(event.data.get("url"));
        String fileName = Utils.extractFileNameFromUrl(url);

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
        metadata.put("ProcessingTime", Utils.getCurrentUtcTime());
        metadata.put("ExpiryTimestamp", Utils.getExpiryTimestamp(EXPIRY_DAYS));

        return metadata;
    }
}
