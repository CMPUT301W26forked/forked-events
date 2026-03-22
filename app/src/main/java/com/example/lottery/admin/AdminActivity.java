package com.example.lottery.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lottery.R;

/**
 * Entry point for admin users. Hosts the AdminDashboardFragment.
 */
public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, new AdminDashboardFragment())
                    .commit();
        }
    }
}
