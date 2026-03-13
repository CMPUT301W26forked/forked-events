package com.example.lottery.Entrant.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;

public class QrEventDetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnShowQr = view.findViewById(R.id.btnShowQr);

        TextView tvHeaderTitle = view.findViewById(R.id.tvTitle);
        TextView tvEventName = view.findViewById(R.id.tvEventName);
        TextView tvStatusTag = view.findViewById(R.id.tvStatusTag);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvTotalSpots = view.findViewById(R.id.tvTotalSpots);
        TextView tvWaitlist = view.findViewById(R.id.tvWaitlist);
        TextView tvConfirmed = view.findViewById(R.id.tvConfirmed);
        TextView tvEventDates = view.findViewById(R.id.tvEventDates);
        TextView tvLocation = view.findViewById(R.id.tvLocation);
        TextView tvOrganizer = view.findViewById(R.id.tvOrganizer);

        LinearLayout lotterySection = view.findViewById(R.id.lotterySection);
        MaterialButton btnJoin = view.findViewById(R.id.btnJoin);
        ImageView ivEventPoster = view.findViewById(R.id.ivEventPoster);

        Bundle args = getArguments();

        if (args != null) {
            String eventId = args.getString("eventId", "");
            String title = args.getString("title", "");
            String status = args.getString("status", "");
            String description = args.getString("description", "");
            String location = args.getString("location", "");
            String date = args.getString("date", "");
            String spots = args.getString("spots", "");
            String lotteryInfo = args.getString("lotteryInfo", "");
            String joinedInfo = args.getString("joinedInfo", "");

            tvHeaderTitle.setText("Event Details");
            tvEventName.setText(title);
            tvStatusTag.setText(status);
            tvDescription.setText(description);
            tvEventDates.setText(date);
            tvLocation.setText(location);
            tvOrganizer.setText("Scanned Event");

            // "20 spots available" -> show only 20 if possible
            tvTotalSpots.setText(extractLeadingNumber(spots));

            // "47 Joined" -> show 47
            tvWaitlist.setText(extractLeadingNumber(joinedInfo));

            // Since scanned event does not currently provide confirmed count,
            // keep it 0 for now
            tvConfirmed.setText("0");

            // QR flow should not show "show QR" again here
            btnShowQr.setVisibility(View.GONE);

            // Poster is optional; keep placeholder if none
            ivEventPoster.setImageResource(R.drawable.ic_launcher_background);

            // Update join button text for scanned flow
            btnJoin.setText("Join Waitlist");

            btnJoin.setOnClickListener(v -> {
                // For now this is just UI feedback/navigation placeholder.
                // If you want, next I can connect this directly to Firestore sign-up.
                btnJoin.setText("Joined");
                btnJoin.setEnabled(false);
            });

            // lotteryInfo is not directly shown in this shared layout except inside lottery section.
            // We keep lottery section visible because this screen is about joining via QR.
            lotterySection.setVisibility(View.VISIBLE);
        }

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }

    private String extractLeadingNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "0";
        }

        StringBuilder number = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c)) {
                number.append(c);
            } else if (number.length() > 0) {
                break;
            }
        }

        return number.length() > 0 ? number.toString() : "0";
    }
}