package com.scor.bulktransfer.models;

import java.util.Date;
import java.util.Map;

public class EventSchema {

    public String topic;
    public String subject;
    public String source;
    public String eventType;
    public Date eventTime;
    public String id;
    public String dataVersion;
    public String metadataVersion;
    public Map<String, Object> data;
    public Map<String, String> metadata;
}