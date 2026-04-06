package com.example.lottery.organizer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for US 02.06.05 - Exporting enrolled list to CSV.
 */
public class ExportEnrolledListToCsvTest {

    /**
     * US 02.06.05 - Test exporting enrolled list to CSV format.
     */
    @Test
    public void exportToCsv_generatesCorrectFormat() {
        EventRepo repo = mock(EventRepo.class);
        CsvExportService service = new CsvExportService(repo);

        List<String> enrolledEntrantIds = Arrays.asList("u1", "u2", "u3");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(enrolledEntrantIds);
            return null;
        }).when(repo).getRegisteredEntrantIds(anyString(), any());

        final String[] result = new String[1];
        service.exportEnrolledList("event123", new RepoCallback<String>() {
            @Override
            public void onSuccess(String csv) {
                result[0] = csv;
            }
            @Override
            public void onError(Exception e) {}
        });

        assertNotNull(result[0]);
        assertTrue(result[0].contains("event123"));
        assertTrue(result[0].contains("u1"));
        assertTrue(result[0].contains("u2"));
        assertTrue(result[0].contains("u3"));
    }

    /**
     * US 02.06.05 - Test CSV export with headers.
     */
    @Test
    public void exportToCsv_includesHeaders() {
        EventRepo repo = mock(EventRepo.class);
        CsvExportService service = new CsvExportService(repo);

        List<String> enrolledEntrantIds = Arrays.asList("u1");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(enrolledEntrantIds);
            return null;
        }).when(repo).getRegisteredEntrantIds(anyString(), any());

        final String[] result = new String[1];
        service.exportEnrolledList("event456", new RepoCallback<String>() {
            @Override
            public void onSuccess(String csv) {
                result[0] = csv;
            }
            @Override
            public void onError(Exception e) {}
        });

        assertTrue(result[0].startsWith("Event ID,Entrant ID"));
    }

    /**
     * US 02.06.05 - Test CSV export with empty list.
     */
    @Test
    public void exportToCsv_emptyList_returnsHeadersOnly() {
        EventRepo repo = mock(EventRepo.class);
        CsvExportService service = new CsvExportService(repo);

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(Arrays.asList());
            return null;
        }).when(repo).getRegisteredEntrantIds(anyString(), any());

        final String[] result = new String[1];
        service.exportEnrolledList("event789", new RepoCallback<String>() {
            @Override
            public void onSuccess(String csv) {
                result[0] = csv;
            }
            @Override
            public void onError(Exception e) {}
        });

        assertEquals("Event ID,Entrant ID\n", result[0]);
    }

    /**
     * US 02.06.05 - Test CSV export propagates errors.
     */
    @Test
    public void exportToCsv_propagatesErrors() {
        EventRepo repo = mock(EventRepo.class);
        CsvExportService service = new CsvExportService(repo);

        Exception expectedError = new RuntimeException("Network error");
        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onError(expectedError);
            return null;
        }).when(repo).getRegisteredEntrantIds(anyString(), any());

        final Exception[] capturedError = new Exception[1];
        service.exportEnrolledList("event123", new RepoCallback<String>() {
            @Override
            public void onSuccess(String csv) {}
            @Override
            public void onError(Exception e) {
                capturedError[0] = e;
            }
        });

        assertEquals(expectedError, capturedError[0]);
    }

    /**
     * Service class for CSV export
     */
    static class CsvExportService {
        private final EventRepo repo;

        public CsvExportService(EventRepo repo) {
            this.repo = repo;
        }

        public void exportEnrolledList(String eventId, RepoCallback<String> callback) {
            repo.getRegisteredEntrantIds(eventId, new RepoCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> entrantIds) {
                    StringBuilder csv = new StringBuilder();
                    csv.append("Event ID,Entrant ID\n");
                    for (String entrantId : entrantIds) {
                        csv.append(eventId).append(",").append(entrantId).append("\n");
                    }
                    callback.onSuccess(csv.toString());
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        }
    }
}
