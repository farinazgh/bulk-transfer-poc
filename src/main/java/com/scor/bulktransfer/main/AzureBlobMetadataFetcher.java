package com.scor.bulktransfer.main;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;

import java.util.Map;

public class AzureBlobMetadataFetcher {
    public static void main(String[] args) {
        String STORAGE_CONNECTION_STRING = System.getenv("STORAGE_CONNECTION_STRING");

        String containerName = "lyra";
        String blobName = "test.txt";

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(STORAGE_CONNECTION_STRING).buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        BlobProperties properties = blobClient.getProperties();
        Map<String, String> metadata = properties.getMetadata();
        metadata.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}

