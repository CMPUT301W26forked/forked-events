package com.example.lottery;


import android.app.AlertDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.example.lottery.Repo.WaitlistCallback;
import com.example.lottery.Service.WaitlistService;


/**
 * Displays event details and allows entrants to join or leave the waiting list.
 * Also handles the option to stay on the waiting list.
 * The following user stories are implemented:
 * US 01.01.01, US 01.01.02, US 01.05.01, US 01.05.04
 * Outstanding issues:
 * -If "status" can be any value except for any variation of "open" or "closed" it will cause problems in joinWaitList
 * -Double dialog bug in joinWaitList
 */
public class EventDetailsFragment extends Fragment {
    private TextView tvEventName;
    private TextView tvEventLocation;
    private TextView tvEventDates;
    private TextView tvStatusTag;
    private TextView tvWaitListCount;
    private TextView tvDescription;
    private MaterialButton btnJoin, btnLeave;
    private ImageButton btnBack;
    private WaitlistService waitlistService;
    private String eventId;
    private String userId;
    private String eventStatus;

    /**
     * Creates new instance of EventDetailsFragment given the event data as arguments.
     * @param eventId ID of the event (from FireStore)
     * @param title Title of the event.
     * @param description Description of the event.
     * @param location The location where event takes place.
     * @param date The date of the event.
     * @param status The current registration status (Open/Closed).
     * @return new EventDetailsFragment with the arguments provided.
     */

    public static EventDetailsFragment newInstance(String eventId, String title, String description, String location, String date, String status) {
    EventDetailsFragment fragment = new EventDetailsFragment();
    Bundle args = new Bundle();
    args.putString("eventId", eventId);
    args.putString("title", title);
    args.putString("description", description);
    args.putString("location", location);
    args.putString("date", date);
    args.putString("status", status);
    fragment.setArguments(args);
    return fragment;
    }

    /**
     * Loads the fragment layout, connects the UI to the variables,
     * gets device id (used to identify user), sets up the button click actions
     * and receives event details from fragment arguments.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return View of the fragment UI
     */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        userId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        waitlistService = new WaitlistService();

        tvEventName = view.findViewById(R.id.tvEventName);
        tvStatusTag = view.findViewById(R.id.tvStatusTag);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvEventDates = view.findViewById(R.id.tvEventDates);
        tvEventLocation = view.findViewById(R.id.tvEventLocation);
        tvWaitListCount = view.findViewById(R.id.tvWaitListCount);
        btnJoin = view.findViewById(R.id.btnJoin);
        btnLeave = view.findViewById(R.id.btnLeave);
        btnBack = view.findViewById(R.id.btnBack);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId", "");
            tvEventName.setText(args.getString("title", ""));
            tvDescription.setText(args.getString("description", ""));
            tvEventDates.setText(args.getString("date", ""));
            tvEventLocation.setText(args.getString("location", ""));
            eventStatus = args.getString("status", "Open");
            tvStatusTag.setText(eventStatus);
        }

        btnJoin.setOnClickListener(v ->joinWaitList());
        btnLeave.setOnClickListener(v -> leaveWaitList());
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnJoin.setVisibility(View.GONE);
        btnLeave.setVisibility(View.GONE);
        waitListStatus();
        return view;
    }

    private void waitListStatus() {
        waitlistService.getWaitListCount(eventId, new WaitlistCallback<Long>() {
            @Override public void onSuccess (Long count){
                tvWaitListCount.setText(String.valueOf(count));
            }
            @Override public void onError (Exception e){
                tvWaitListCount.setText("0");
            }
        });

        waitlistService.checkWaitListStatus(eventId, userId, new WaitlistCallback<Boolean>() {
            @Override public void onSuccess(Boolean isOnWaitList) {showCorrectButton(isOnWaitList);}
            @Override public void onError(Exception e) {showCorrectButton(false);}
        });



    }

    private void joinWaitList() {
        if(!"Open".equalsIgnoreCase(eventStatus)){
            Toast.makeText(requireContext(), "Registration is closed.", Toast.LENGTH_SHORT).show();
            return;
        }
        waitlistService.joinWaitList(eventId, userId, new WaitlistCallback<Void>(){
            @Override public void onSuccess(Void r) {showCorrectButton(true); waitListStatus();}
            @Override public void onError(Exception e){}
        });
    }

    private void leaveWaitList(){
        new AlertDialog.Builder(requireContext()).setTitle("Leave Waiting List").setMessage("Leave waiting list?").setPositiveButton("Yes", (dialog, which) ->
            waitlistService.leaveWaitList(eventId, userId, new WaitlistCallback<Void>() {
            @Override public void onSuccess(Void r) { showCorrectButton(false); waitListStatus(); }
            @Override public void onError(Exception e) {} })).setNegativeButton("Cancel", null).show();
    }

    /**
     * Asks user if they want to stay on waiting list after not being selected to participate in the event.
     */

    public void showStayInList(){
        new AlertDialog.Builder(requireContext()).setTitle("Not Selected").setMessage("Stay in waiting list?").setPositiveButton("Yes", (dialog, which) ->
                waitlistService.stayInList(eventId, userId, new WaitlistCallback<Void>() {
                    @Override public void onSuccess(Void r) {}
                    @Override public void onError(Exception e) {} })).setNegativeButton("No", (dialog, which) -> leaveWaitList()).setCancelable(false).show();
    }
    private void showCorrectButton(boolean isOnWaitlist) {
        if (isOnWaitlist) {
            btnJoin.setVisibility(View.GONE);
            btnLeave.setVisibility(View.VISIBLE);
        } else {
            btnJoin.setVisibility(View.VISIBLE);
            btnLeave.setVisibility(View.GONE);
        }
    }
}
