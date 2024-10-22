package com.scor.bulktransfer.main;


import com.scor.bulktransfer.services.StorageService;

public class StorageServiceTestMain {

    public static void main(String[] args) {

        StorageService storageService = StorageService.getInstance();

        String eventId = "event98765";
        String metadataJson = "{\"name\":\"Sample Event\",\"description\":\"This is a sample event for testing.\"}";

        try {
            boolean isProcessed = storageService.isEventProcessed(eventId);
            if (isProcessed) {
                System.out.println("Event '" + eventId + "' has already been processed.");
            } else {
                storageService.logDataToTableStorage(metadataJson, eventId,null);
            }

            isProcessed = storageService.isEventProcessed(eventId);
            System.out.println("Is event processed after logging? " + isProcessed);
        } catch (Exception e) {
            System.err.println("An error occurred during StorageService operations:");
            e.printStackTrace();
        }
    }
}
