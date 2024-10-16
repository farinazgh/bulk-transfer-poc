package com.scor.bulktransfer.services;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.microsoft.azure.functions.ExecutionContext;
import com.scor.bulktransfer.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StorageServiceTest {

    @Mock
    private TableClient tableClientMock;

    @Mock
    private TableClient processedEventsClientMock;

    @Mock
    private ExecutionContext contextMock;

    @Mock
    private Logger loggerMock;

    private StorageService storageService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(contextMock.getLogger()).thenReturn(loggerMock);
        storageService = new StorageService(tableClientMock, processedEventsClientMock);
    }

    @Test
    public void testLogDataToTableStorage_Success() {
        String metadataJson = "{\"key\":\"value\"}";
        String eventId = "event123";

        doNothing().when(tableClientMock).upsertEntity(any(TableEntity.class));
        doNothing().when(processedEventsClientMock).createEntity(any(TableEntity.class));
        doNothing().when(loggerMock).info(anyString());

        storageService.logDataToTableStorage(metadataJson, eventId, contextMock);

        verify(tableClientMock, times(1)).upsertEntity(any(TableEntity.class));
        verify(processedEventsClientMock, times(1)).createEntity(any(TableEntity.class));
        verify(contextMock.getLogger(), times(1))
                .info(String.format("Data logged with PartitionKey: %s, RowKey: %s",
                        storageService.generatePartitionKey(eventId),
                        Utils.sanitizeForTableStorage(eventId)));
    }


    @Test
    public void testIsEventProcessed_EventExists() {
        String eventId = "event123";
        TableEntity existingEntity = new TableEntity("ProcessedEventsPartition", eventId);

        when(processedEventsClientMock.getEntity("ProcessedEventsPartition", eventId))
                .thenReturn(existingEntity);

        boolean result = storageService.isEventProcessed(eventId);
        assertTrue(result);
    }

    @Test
    public void testIsEventProcessed_EventDoesNotExist() throws IOException {
        String eventId = "event123";

        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.getStatusCode()).thenReturn(404);
        when(mockResponse.getHeaders()).thenReturn(new HttpHeaders());

        TableServiceException exception = new TableServiceException("Not Found", mockResponse);

        when(processedEventsClientMock.getEntity("ProcessedEventsPartition", eventId))
                .thenThrow(exception);

        boolean result = storageService.isEventProcessed(eventId);
        assertFalse(result);
    }

    @Test
    public void testIsEventProcessed_TableServiceException() throws IOException {
        String eventId = "event123";

        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.getStatusCode()).thenReturn(500);
        when(mockResponse.getHeaders()).thenReturn(new HttpHeaders());

        TableServiceException exception = new TableServiceException("Internal Server Error", mockResponse);

        when(processedEventsClientMock.getEntity("ProcessedEventsPartition", eventId))
                .thenThrow(exception);

        TableServiceException thrown = assertThrows(TableServiceException.class, () -> {
            storageService.isEventProcessed(eventId);
        });

        assertEquals(500, thrown.getResponse().getStatusCode());
        assertEquals("Internal Server Error", thrown.getMessage());
    }

    @Test
    public void testMarkEventAsProcessed_Success() {
        String eventId = "event123";

        doNothing().when(processedEventsClientMock).createEntity(any(TableEntity.class));

        storageService.markEventAsProcessed(eventId);

        ArgumentCaptor<TableEntity> captor = ArgumentCaptor.forClass(TableEntity.class);
        verify(processedEventsClientMock, times(1)).createEntity(captor.capture());

        TableEntity capturedEntity = captor.getValue();
        assertEquals("ProcessedEventsPartition", capturedEntity.getPartitionKey());
        assertEquals(eventId, capturedEntity.getRowKey());
        assertNotNull(capturedEntity.getProperty("ProcessedAt"));
    }
}
