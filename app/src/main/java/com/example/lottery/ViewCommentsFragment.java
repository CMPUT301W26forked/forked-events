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
 * fragment for organizer comment view
 */
public class ViewCommentsFragment extends Fragment {
    private String eventId;
    private String eventName;
    private CommentsAdapter adapter;
    private RecyclerView rvComments;
    private List<EventComment> commentlist;
    private EventService service;
    private TextView tvListTitle;

    public static ViewCommentsFragment newInstance(String eventId, String eventName) {
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        args.putString("eventName", eventName);
        fragment.setArguments(args);
        return fragment;
    }

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
     * reload
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
     * delete a comment
     * @param comment comment to delete
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