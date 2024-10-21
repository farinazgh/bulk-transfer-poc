package com.scor.bulktransfer.main;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;

import java.util.Map;

public class AzureBlobMetadataFetcher {
    public static void main(String[] args) {
        // Connection string to your storage account
        String connectionString = "";
        String containerName = "lyra";
        String blobName = "salut.txt";

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        BlobProperties properties = blobClient.getProperties();
        Map<String, String> metadata = properties.getMetadata();
        metadata.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}

