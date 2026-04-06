package com.example.lottery.Admin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.lottery.Event;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for US 03.04.01 - Admin browsing all events.
 */
public class AdminBrowseEventsTest {

    /**
     * US 03.04.01 - Test admin can retrieve all events.
     */
    @Test
    public void browseEvents_returnsAllEvents() {
        AdminEventRepository repo = mock(AdminEventRepository.class);
        AdminBrowseService service = new AdminBrowseService(repo);

        List<Event> allEvents = new ArrayList<>();
        Event event1 = new Event();
        event1.setEventId("e1");
        event1.setName("Event 1");
        allEvents.add(event1);

        Event event2 = new Event();
        event2.setEventId("e2");
        event2.setName("Event 2");
        allEvents.add(event2);

        doAnswer(inv -> {
            AdminRepoCallback<List<Event>> cb = inv.getArgument(0);
            cb.onSuccess(allEvents);
            return null;
        }).when(repo).getAllEvents(any());

        final List<Event>[] result = new List[1];
        service.browseAllEvents(new AdminRepoCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                result[0] = events;
            }
            @Override
            public void onError(Exception e) {}
        });

        assertNotNull(result[0]);
        assertEquals(2, result[0].size());
        assertEquals("e1", result[0].get(0).getEventId());
        assertEquals("e2", result[0].get(1).getEventId());
    }


    /**
     * Mock callback interface for admin repo
     */
    interface AdminRepoCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    /**
     * Mock repository interface
     */
    interface AdminEventRepository {
        void getAllEvents(AdminRepoCallback<List<Event>> callback);
    }

    /**
     * Service class for admin browsing
     */
    static class AdminBrowseService {
        private final AdminEventRepository repo;

        public AdminBrowseService(AdminEventRepository repo) {
            this.repo = repo;
        }

        public void browseAllEvents(AdminRepoCallback<List<Event>> callback) {
            repo.getAllEvents(callback);
        }
    }
}
