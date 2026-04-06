package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.organizer.EventService;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.PosterStorageService;
import com.example.lottery.organizer.RepoCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for the organizer to view and manage comments for a specific event.
 * Displays comments in a nested format to show replies.
 */
public class ViewCommentsFragment extends Fragment {
    private String eventId;
    private String eventName;
    private CommentsAdapter adapter;
    private RecyclerView rvComments;
    private List<EventComment> commentlist;
    private EventService service;
    private TextView tvListTitle;

    /**
     * Creates a new instance of ViewCommentsFragment with event details.
     * @param eventId The ID of the event to view comments for.
     * @param eventName The name of the event.
     * @return A new instance of ViewCommentsFragment.
     */
    public static ViewCommentsFragment newInstance(String eventId, String eventName) {
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        args.putString("eventName", eventName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the fragment layout and initializes UI components and event service.
     */
    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        if (getArguments() != null) {
            eventId = getArguments().getString("event_id");
            eventName = getArguments().getString("eventName", "Event");
        }

        service = new EventService(new FSEventRepo(), new PosterStorageService());
        commentlist = new ArrayList<>();

        tvListTitle = view.findViewById(R.id.tvListTitle);
        rvComments = view.findViewById(R.id.rvComments);

        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentsAdapter(commentlist, this::deleteComment);
        rvComments.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        loadComments();

        return view;
    }

    /**
     * Fetches comments from the database and updates the RecyclerView.
     */
    private void loadComments() {
        tvListTitle.setText(eventName + " Comments");
        service.getComments(eventId, new RepoCallback<List<EventComment>>() {
            @Override
            public void onSuccess(List<EventComment> comments) {
                commentlist.clear();
                commentlist.addAll(buildNestedDisplayList(comments));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a specific comment from the database.
     * @param comment The comment object to be deleted.
     */
    private void deleteComment(EventComment comment) {
        service.deleteComment(eventId, comment.getCommentId(), new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(requireContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                loadComments();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                loadComments();
            }
        });
    }

    /**
     * Organizes a flat list of comments into a nested display list based on parent-child relationships.
     * @param allComments The flat list of all comments for the event.
     * @return A list of comments sorted for nested display.
     */
    private List<EventComment> buildNestedDisplayList(List<EventComment> allComments) {
        List<EventComment> displayList = new ArrayList<>();
        Map<String, List<EventComment>> childrenMap = new HashMap<>();
        List<EventComment> parentComments = new ArrayList<>();

        for (EventComment comment : allComments) {
            String parentId = comment.getParentCommentId();

            if (parentId == null || parentId.trim().isEmpty()) {
                parentComments.add(comment);
            } else {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
            }
        }

        for (EventComment parent : parentComments) {
            parent.setDepth(0);
            displayList.add(parent);
            addRepliesRecursively(parent, childrenMap, displayList, 1);
        }

        return displayList;
    }

    /**
     * Recursively adds replies to the display list.
     * @param parent The parent comment.
     * @param childrenMap Map containing lists of child comments keyed by their parent ID.
     * @param displayList The list to which comments are being added.
     * @param depth The current nesting depth level.
     */
    private void addRepliesRecursively(EventComment parent,
                                       Map<String, List<EventComment>> childrenMap,
                                       List<EventComment> displayList,
                                       int depth) {
        if (parent.getCommentId() == null) {
            return;
        }

        List<EventComment> replies = childrenMap.get(parent.getCommentId());
        if (replies == null) {
            return;
        }

        for (EventComment reply : replies) {
            reply.setDepth(depth);
            displayList.add(reply);
            addRepliesRecursively(reply, childrenMap, displayList, depth + 1);
        }
    }
}
