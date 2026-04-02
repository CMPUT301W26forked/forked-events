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

    /**
     * Inflates event details layout and populates UI using data passed via arguments.
     * Handles QR-specific UI behavior such as hiding QR button and enabling waitlist join.
     */
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

        // Populate UI fields if arguments are provided
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

            if (tvHeaderTitle != null) {
                tvHeaderTitle.setText("Event Details");
            }
            tvEventName.setText(title);
            tvStatusTag.setText(status);
            tvDescription.setText(description);
            tvEventDates.setText(date);
            tvLocation.setText(location);
            tvOrganizer.setText("Scanned Event");

            // Extract numeric values from text fields for display
            tvTotalSpots.setText(extractLeadingNumber(spots));
            tvWaitlist.setText(extractLeadingNumber(joinedInfo));

            // Default confirmed count for scanned event
            tvConfirmed.setText("0");

            // Hide QR button since user already came via QR
            btnShowQr.setVisibility(View.GONE);

            // Set default placeholder image
            ivEventPoster.setImageResource(R.drawable.ic_launcher_background);

            // Configure join button behavior
            btnJoin.setText("Join Waitlist");
            btnJoin.setOnClickListener(v -> {
                btnJoin.setText("Joined");
                btnJoin.setEnabled(false);
            });

            // Ensure lottery section is visible for QR-based joining
            lotterySection.setVisibility(View.VISIBLE);
        }

        // Handle back navigation
        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }

    /**
     * Extracts the first numeric value from a string.
     * Returns "0" if no digits are found or input is empty.
     */
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
