package com.scor.bulktransfer.main;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class BlobConsumer {

    public static void main(String[] args) throws Exception {
        String blobUrl = "";
        String receivedChecksum = "";

        String downloadFilePath = "";
        downloadBlob(blobUrl, downloadFilePath);

        String computedChecksum = computeChecksum(downloadFilePath);

        System.out.println("Computed checksum: " + computedChecksum);
        System.out.println("Received checksum: " + receivedChecksum);

        if (computedChecksum.equals(receivedChecksum)) {
            System.out.println("Checksum validation succeeded. File is intact.");
        } else {
            System.out.println("Checksum validation failed. File may be corrupted.");
        }
    }

    private static void downloadBlob(String blobUrl, String downloadFilePath) {
        BlobClient blobClient = new BlobClientBuilder()
                .endpoint(blobUrl)
                .buildClient();

        blobClient.downloadToFile(downloadFilePath, true);
    }

    private static String computeChecksum(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        InputStream fis = Files.newInputStream(Paths.get(filePath));

        byte[] byteArray = new byte[1024];
        int bytesCount;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();

        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
