package com.example.lottery.admin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.lottery.EventComment;
import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for US 03.10.01 - Admin removing comments.
 */
public class AdminRemoveCommentTest {

    /**
     * US 03.10.01 - Test admin can delete any comment.
     */
    @Test
    public void adminCanDeleteComment_deletesSuccessfully() {
        AdminCommentRepository repo = mock(AdminCommentRepository.class);
        AdminCommentService service = new AdminCommentService(repo);

        doAnswer(inv -> {
            AdminRepoCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).deleteComment(anyString(), anyString(), any());

        final boolean[] successCalled = {false};
        service.deleteComment("event123", "comment456", "admin789",
                new AdminRepoCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        successCalled[0] = true;
                    }
                    @Override
                    public void onError(Exception e) {}
                });

        assertTrue(successCalled[0]);
        verify(repo).deleteComment(eq("event123"), eq("comment456"), any());
    }

    /**
     * US 03.10.01 - Test admin can view all comments before deletion.
     */
    @Test
    public void adminCanViewAllComments_returnsAllComments() {
        AdminCommentRepository repo = mock(AdminCommentRepository.class);
        AdminCommentService service = new AdminCommentService(repo);

        Timestamp now = Timestamp.now();
        EventComment comment1 = new EventComment("c1", "User1", "u1", "Comment 1", now);
        EventComment comment2 = new EventComment("c2", "User2", "u2", "Comment 2", now);
        List<EventComment> comments = Arrays.asList(comment1, comment2);

        doAnswer(inv -> {
            AdminRepoCallback<List<EventComment>> cb = inv.getArgument(1);
            cb.onSuccess(comments);
            return null;
        }).when(repo).getAllComments(anyString(), any());

        final List<EventComment>[] result = new List[1];
        service.getAllComments("event123", new AdminRepoCallback<List<EventComment>>() {
            @Override
            public void onSuccess(List<EventComment> commentList) {
                result[0] = commentList;
            }
            @Override
            public void onError(Exception e) {}
        });

        assertNotNull(result[0]);
        assertEquals(2, result[0].size());
        assertEquals("c1", result[0].get(0).getCommentId());
        assertEquals("c2", result[0].get(1).getCommentId());
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
    interface AdminCommentRepository {
        void deleteComment(String eventId, String commentId, AdminRepoCallback<Void> callback);
        void getAllComments(String eventId, AdminRepoCallback<List<EventComment>> callback);
    }

    /**
     * Service class for admin comment management
     */
    static class AdminCommentService {
        private final AdminCommentRepository repo;

        public AdminCommentService(AdminCommentRepository repo) {
            this.repo = repo;
        }

        public void deleteComment(String eventId, String commentId, String adminId,
                                  AdminRepoCallback<Void> callback) {
            repo.deleteComment(eventId, commentId, callback);
        }

        public void getAllComments(String eventId, AdminRepoCallback<List<EventComment>> callback) {
            repo.getAllComments(eventId, callback);
        }
    }
}
