package com.scor.bulktransfer.models;

public class MessagePayload {
    private String blobURL;
    private String checksum;
    private String fileSize;
    private String blobType;

    public MessagePayload() {
    }

    public MessagePayload(String blobURL, String checksum, String fileSize, String blobType) {
        this.blobURL = blobURL;
        this.checksum = checksum;
        this.fileSize = fileSize;
        this.blobType = blobType;
    }

    public String getBlobURL() {
        return blobURL;
    }

    public void setBlobURL(String blobURL) {
        this.blobURL = blobURL;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getBlobType() {
        return blobType;
    }

    public void setBlobType(String blobType) {
        this.blobType = blobType;
    }
}
