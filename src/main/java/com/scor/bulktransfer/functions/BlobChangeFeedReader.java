package com.scor.bulktransfer.functions;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

public class BlobChangeFeedReader {

    private static final String STORAGE_CONNECTION_STRING = "";

    @FunctionName("BlobChangeFeedReaderFunction")
    public void run(
            @TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo,
            final ExecutionContext context) {

        context.getLogger().info("Blob Change Feed Reader function executed at: " + java.time.LocalDateTime.now());

        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(STORAGE_CONNECTION_STRING)
                    .buildClient();

            BlobContainerClient changeFeedContainer = blobServiceClient.getBlobContainerClient("$blobchangefeed");

            if (changeFeedContainer.exists()) {
                for (BlobItem blobItem : changeFeedContainer.listBlobs()) {
                    context.getLogger().info("Found Change Feed blob: " + blobItem.getName());

                    String blobContent = new String(changeFeedContainer.getBlobClient(blobItem.getName())
                            .downloadContent()
                            .toBytes());

                    context.getLogger().info("Change Feed Data: " + blobContent);
                }
            } else {
                context.getLogger().warning("Change Feed container does not exist.");
            }
        } catch (Exception ex) {
            context.getLogger().severe("An error occurred: " + ex.getMessage());
        }
    }
}
