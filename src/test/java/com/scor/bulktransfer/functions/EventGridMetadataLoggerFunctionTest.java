package com.scor.bulktransfer.functions;


import com.microsoft.azure.functions.ExecutionContext;
import com.scor.bulktransfer.models.EventSchema;
import com.scor.bulktransfer.services.JsonService;
import com.scor.bulktransfer.services.MessagingService;
import com.scor.bulktransfer.services.MetadataService;
import com.scor.bulktransfer.services.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class EventGridMetadataLoggerFunctionTest {

    @Mock
    private MetadataService metadataServiceMock;

    @Mock
    private JsonService jsonServiceMock;

    @Mock
    private StorageService storageServiceMock;

    @Mock
    private MessagingService messagingServiceMock;

    @Mock
    private ExecutionContext contextMock;

    @Mock
    Logger loggerMock;

    @InjectMocks
    private EventGridMetadataLoggerFunction function;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock the logger
        when(contextMock.getLogger()).thenReturn(loggerMock);
    }

    @Test
    public void testRun_SuccessfulProcessing() {
        // Arrange
        EventSchema event = new EventSchema();
        event.id = "event123";
        // Populate other necessary fields of EventSchema as needed

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("key", "value");

        String metadataJson = "{\"key\":\"value\"}";

        when(storageServiceMock.isEventProcessed("event123")).thenReturn(false);
        when(metadataServiceMock.createMetadata(event)).thenReturn(metadataMap);
        when(jsonServiceMock.convertToJson(metadataMap)).thenReturn(metadataJson);

        // Act
        function.run(event, contextMock);

        // Assert
        verify(storageServiceMock, times(1)).isEventProcessed("event123");
        verify(metadataServiceMock, times(1)).createMetadata(event);
        verify(jsonServiceMock, times(1)).convertToJson(metadataMap);
        verify(storageServiceMock, times(1)).logDataToTableStorage(metadataJson, "event123", contextMock);
        verify(messagingServiceMock, times(1)).sendMessage(metadataJson, contextMock);
        verify(loggerMock, times(1)).info("Successfully processed Event ID: event123");
    }

    @Test
    public void testRun_DuplicateEvent_Skipped() {
        // Arrange
        EventSchema event = new EventSchema();
        event.id = "event123";

        when(storageServiceMock.isEventProcessed("event123")).thenReturn(true);

        // Act
        function.run(event, contextMock);

        // Assert
        verify(storageServiceMock, times(1)).isEventProcessed("event123");
        verify(loggerMock, times(1)).info("Event already processed: event123");
        verifyNoMoreInteractions(metadataServiceMock, jsonServiceMock, storageServiceMock, messagingServiceMock);
    }

/*    @Test
    public void testRun_NullEvent_ThrowsException() {
        // Arrange
        EventSchema event = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            function.run(event, contextMock);
        });

        assertEquals("Event cannot be null.", exception.getMessage());
        verify(loggerMock, times(1)).severe("Received null event.");
        verifyNoMoreInteractions(metadataServiceMock, jsonServiceMock, storageServiceMock, messagingServiceMock);
    }*/

    @Test
    public void testRun_MissingEventId_ThrowsException() {
        // Arrange
        EventSchema event = new EventSchema();
        event.id = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            function.run(event, contextMock);
        });

        assertEquals("Event ID is required.", exception.getMessage());
        verify(loggerMock, times(1)).severe("Event ID is missing.");
        verifyNoMoreInteractions(metadataServiceMock, jsonServiceMock, storageServiceMock, messagingServiceMock);
    }

    @Test
    public void testRun_MetadataServiceThrowsException() {
        // Arrange
        EventSchema event = new EventSchema();
        event.id = "event123";

        when(storageServiceMock.isEventProcessed("event123")).thenReturn(false);
        when(metadataServiceMock.createMetadata(event)).thenThrow(new RuntimeException("Metadata creation failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            function.run(event, contextMock);
        });

        assertEquals("Metadata creation failed", exception.getMessage());
        verify(storageServiceMock, times(1)).isEventProcessed("event123");
        verify(metadataServiceMock, times(1)).createMetadata(event);
        verify(loggerMock, times(1)).severe("Error processing EventGrid event ID event123: Metadata creation failed");
        verifyNoMoreInteractions(jsonServiceMock, storageServiceMock, messagingServiceMock);
    }

    @Test
    public void testRun_StorageServiceThrowsException() {
        // Arrange
        EventSchema event = new EventSchema();
        event.id = "event123";

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("key", "value");

        String metadataJson = "{\"key\":\"value\"}";

        when(storageServiceMock.isEventProcessed("event123")).thenReturn(false);
        when(metadataServiceMock.createMetadata(event)).thenReturn(metadataMap);
        when(jsonServiceMock.convertToJson(metadataMap)).thenReturn(metadataJson);
        doThrow(new RuntimeException("Storage failure")).when(storageServiceMock).logDataToTableStorage(metadataJson, "event123", contextMock);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            function.run(event, contextMock);
        });

        assertEquals("Storage failure", exception.getMessage());
        verify(storageServiceMock, times(1)).isEventProcessed("event123");
        verify(metadataServiceMock, times(1)).createMetadata(event);
        verify(jsonServiceMock, times(1)).convertToJson(metadataMap);
        verify(storageServiceMock, times(1)).logDataToTableStorage(metadataJson, "event123", contextMock);
        verify(loggerMock, times(1)).severe("Error processing EventGrid event ID event123: Storage failure");
        verifyNoMoreInteractions(messagingServiceMock);
    }

    @Test
    public void testRun_MessagingServiceThrowsException() {
        // Arrange
        EventSchema event = new EventSchema();
        event.id = "event123";

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("key", "value");

        String metadataJson = "{\"key\":\"value\"}";

        when(storageServiceMock.isEventProcessed("event123")).thenReturn(false);
        when(metadataServiceMock.createMetadata(event)).thenReturn(metadataMap);
        when(jsonServiceMock.convertToJson(metadataMap)).thenReturn(metadataJson);
        doNothing().when(storageServiceMock).logDataToTableStorage(metadataJson, "event123", contextMock);
        doThrow(new RuntimeException("Messaging failure")).when(messagingServiceMock).sendMessage(metadataJson, contextMock);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            function.run(event, contextMock);
        });

        assertEquals("Messaging failure", exception.getMessage());
        verify(storageServiceMock, times(1)).isEventProcessed("event123");
        verify(metadataServiceMock, times(1)).createMetadata(event);
        verify(jsonServiceMock, times(1)).convertToJson(metadataMap);
        verify(storageServiceMock, times(1)).logDataToTableStorage(metadataJson, "event123", contextMock);
        verify(messagingServiceMock, times(1)).sendMessage(metadataJson, contextMock);
        verify(loggerMock, times(1)).severe("Error processing EventGrid event ID event123: Messaging failure");
    }
}
