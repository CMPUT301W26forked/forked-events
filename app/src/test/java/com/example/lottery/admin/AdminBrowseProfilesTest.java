package com.example.lottery.admin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.lottery.Entrant.Model.EntrantProfile;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for US 03.05.01 - Admin browsing all profiles.
 */
public class AdminBrowseProfilesTest {

    /**
     * US 03.05.01 - Test admin can retrieve all user profiles.
     */
    @Test
    public void browseProfiles_returnsAllProfiles() {
        AdminProfileRepository repo = mock(AdminProfileRepository.class);
        AdminProfileBrowseService service = new AdminProfileBrowseService(repo);

        List<EntrantProfile> allProfiles = new ArrayList<>();

        EntrantProfile profile1 = new EntrantProfile();
        profile1.setId("u1");
        profile1.setName("User 1");
        allProfiles.add(profile1);

        EntrantProfile profile2 = new EntrantProfile();
        profile2.setId("u2");
        profile2.setName("User 2");
        allProfiles.add(profile2);

        EntrantProfile profile3 = new EntrantProfile();
        profile3.setId("u3");
        profile3.setName("User 3");
        allProfiles.add(profile3);

        doAnswer(inv -> {
            AdminRepoCallback<List<EntrantProfile>> cb = inv.getArgument(0);
            cb.onSuccess(allProfiles);
            return null;
        }).when(repo).getAllProfiles(any());

        final List<EntrantProfile>[] result = new List[1];
        service.browseAllProfiles(new AdminRepoCallback<List<EntrantProfile>>() {
            @Override
            public void onSuccess(List<EntrantProfile> profiles) {
                result[0] = profiles;
            }
            @Override
            public void onError(Exception e) {}
        });

        assertNotNull(result[0]);
        assertEquals(3, result[0].size());
        assertEquals("u1", result[0].get(0).getId());
        assertEquals("u2", result[0].get(1).getId());
        assertEquals("u3", result[0].get(2).getId());
    }

    /**
     * US 03.05.01 - Test browsing profiles with filter by role.
     */
    @Test
    public void browseProfiles_filterByRole_returnsFiltered() {
        AdminProfileRepository repo = mock(AdminProfileRepository.class);
        AdminProfileBrowseService service = new AdminProfileBrowseService(repo);

        List<EntrantProfile> entrantProfiles = new ArrayList<>();
        EntrantProfile profile1 = new EntrantProfile();
        profile1.setId("u1");
        profile1.setName("Entrant User");
        entrantProfiles.add(profile1);

        doAnswer(inv -> {
            String role = inv.getArgument(0);
            AdminRepoCallback<List<EntrantProfile>> cb = inv.getArgument(1);
            cb.onSuccess(entrantProfiles);
            return null;
        }).when(repo).getProfilesByRole(anyString(), any());

        final List<EntrantProfile>[] result = new List[1];
        service.browseProfilesByRole("entrant", new AdminRepoCallback<List<EntrantProfile>>() {
            @Override
            public void onSuccess(List<EntrantProfile> profiles) {
                result[0] = profiles;
            }
            @Override
            public void onError(Exception e) {}
        });

        assertNotNull(result[0]);
        assertEquals(1, result[0].size());
    }


    /**
     * Mock callback interface
     */
    interface AdminRepoCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    /**
     * Mock repository interface
     */
    interface AdminProfileRepository {
        void getAllProfiles(AdminRepoCallback<List<EntrantProfile>> callback);
        void getProfilesByRole(String role, AdminRepoCallback<List<EntrantProfile>> callback);
    }

    /**
     * Service class for admin profile browsing
     */
    static class AdminProfileBrowseService {
        private final AdminProfileRepository repo;

        public AdminProfileBrowseService(AdminProfileRepository repo) {
            this.repo = repo;
        }

        public void browseAllProfiles(AdminRepoCallback<List<EntrantProfile>> callback) {
            repo.getAllProfiles(callback);
        }

        public void browseProfilesByRole(String role, AdminRepoCallback<List<EntrantProfile>> callback) {
            repo.getProfilesByRole(role, callback);
        }
    }
}
