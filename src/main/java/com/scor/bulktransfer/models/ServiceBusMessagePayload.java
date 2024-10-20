package com.scor.bulktransfer.models;


public class ServiceBusMessagePayload {
    private String eventId;
    private String timestamp;
    private ProducerInfo producer;
    private FileInfo file;
    private MetadataInfo metadata;
    private ProcessingInfo processing;

    public ServiceBusMessagePayload() {}

    public ServiceBusMessagePayload(String eventId, String timestamp, ProducerInfo producer,
                                    FileInfo file, MetadataInfo metadata, ProcessingInfo processing) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.producer = producer;
        this.file = file;
        this.metadata = metadata;
        this.processing = processing;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public ProducerInfo getProducer() { return producer; }
    public void setProducer(ProducerInfo producer) { this.producer = producer; }

    public FileInfo getFile() { return file; }
    public void setFile(FileInfo file) { this.file = file; }

    public MetadataInfo getMetadata() { return metadata; }
    public void setMetadata(MetadataInfo metadata) { this.metadata = metadata; }

    public ProcessingInfo getProcessing() { return processing; }
    public void setProcessing(ProcessingInfo processing) { this.processing = processing; }

    public static class ProducerInfo {
        private String producerId;
        private String producerName;
        private String producerType;

        public ProducerInfo() {}

        public ProducerInfo(String producerId, String producerName, String producerType) {
            this.producerId = producerId;
            this.producerName = producerName;
            this.producerType = producerType;
        }

        public String getProducerId() { return producerId; }
        public void setProducerId(String producerId) { this.producerId = producerId; }

        public String getProducerName() { return producerName; }
        public void setProducerName(String producerName) { this.producerName = producerName; }

        public String getProducerType() { return producerType; }
        public void setProducerType(String producerType) { this.producerType = producerType; }
    }

    public static class FileInfo {
        private String fileName;
        private String blobUrl;
        private long fileSize;
        private String fileType;
        private String checksum;

        public FileInfo() {}

        public FileInfo(String fileName, String blobUrl, long fileSize, String fileType, String checksum) {
            this.fileName = fileName;
            this.blobUrl = blobUrl;
            this.fileSize = fileSize;
            this.fileType = fileType;
            this.checksum = checksum;
        }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getBlobUrl() { return blobUrl; }
        public void setBlobUrl(String blobUrl) { this.blobUrl = blobUrl; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public String getChecksum() { return checksum; }
        public void setChecksum(String checksum) { this.checksum = checksum; }
    }

    public static class MetadataInfo {
        private String metadataId;
        private String partitionKey;
        private String rowKey;

        public MetadataInfo() {}

        public MetadataInfo(String metadataId, String partitionKey, String rowKey) {
            this.metadataId = metadataId;
            this.partitionKey = partitionKey;
            this.rowKey = rowKey;
        }

        public String getMetadataId() { return metadataId; }
        public void setMetadataId(String metadataId) { this.metadataId = metadataId; }

        public String getPartitionKey() { return partitionKey; }
        public void setPartitionKey(String partitionKey) { this.partitionKey = partitionKey; }

        public String getRowKey() { return rowKey; }
        public void setRowKey(String rowKey) { this.rowKey = rowKey; }
    }

    public static class ProcessingInfo {
        private String priority;
        private String deadline;

        public ProcessingInfo() {}

        public ProcessingInfo(String priority, String deadline) {
            this.priority = priority;
            this.deadline = deadline;
        }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getDeadline() { return deadline; }
        public void setDeadline(String deadline) { this.deadline = deadline; }
    }
}
