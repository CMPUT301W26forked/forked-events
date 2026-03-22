package com.example.lottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lottery.R;
import java.util.ArrayList;
import java.util.List;

public class ImageModerationFragment extends Fragment {

    private RecyclerView recyclerView;
    private ModerationAdapter adapter;
    private List<ModerationItem> moderationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_moderation, container, false);

        recyclerView = view.findViewById(R.id.rvModerationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        moderationList = new ArrayList<>();
        // Adding dummy data to match the image
        for (int i = 0; i < 10; i++) {
            moderationList.add(new ModerationItem("Moderation by User_X", "Description detail"));
        }

        adapter = new ModerationAdapter(moderationList);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        return view;
    }
}
