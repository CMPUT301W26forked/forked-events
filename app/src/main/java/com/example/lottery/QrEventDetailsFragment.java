package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class QrEventDetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvStatus = view.findViewById(R.id.tvStatusTag);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvLocation = view.findViewById(R.id.tvLocation);
        TextView tvDate = view.findViewById(R.id.tvEventDates);
        TextView tvSpots = view.findViewById(R.id.tvTotalSpots);
        TextView tvLottery = view.findViewById(R.id.lotterySection);
        TextView tvJoined = view.findViewById(R.id.btnJoin);

        Bundle args = getArguments();
        if (args != null) {
            tvTitle.setText(args.getString("title", ""));
            tvStatus.setText(args.getString("status", ""));
            tvDescription.setText(args.getString("description", ""));
            tvLocation.setText(args.getString("location", ""));
            tvDate.setText(args.getString("date", ""));
            tvSpots.setText(args.getString("spots", ""));
            tvLottery.setText(args.getString("lotteryInfo", ""));
            tvJoined.setText(args.getString("joinedInfo", ""));
        }

        return view;
    }
}