package com.example.zooseeker_team54;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.HashMap;
import java.util.List;

/**
 *  Class to represent the functionality of RouteDirectionActivity
 */
public class RouteDirectionActivity extends AppCompatActivity {
    private ViewModel viewModel;
    DirectionsDisplayRecyclerView display;
    DirectionsDisplayNextButton nextBtn;
    Button settingsBtn;

    /**
     * Create the activity from a given savedInstanceState and initialize everything
     * @param savedInstanceState saved instance from before
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_direction);

        Intent intent = getIntent();
        viewModel = new ViewModelProvider(this).get(ViewModel.class);

        // Initialize Recyclerview
        display = new DirectionsDisplayRecyclerView((List<LocEdge>) intent.getSerializableExtra("directions"),
                (HashMap<String, List<LocEdge>>) intent.getSerializableExtra("route"));
        display.setContext(this);
        display.initializeRecyclerView();

        // Initialize New Button
        nextBtn = new DirectionsDisplayNextButton(this, display, viewModel);
        nextBtn.initializeButton();

        //
        settingsBtn = this.findViewById(R.id.settings_button);
        settingsBtn.setOnClickListener(this::onSettingsClicked);
    }

    /**
     * Finishes the activity and goes back to the previous activity
     * @param view
     */
    public void onBackToPlanBtnClicked(View view){ finish(); }

    /**
     * Getter method for the display
     * @return RecyclerView of the directions
     */
    public DirectionsDisplayRecyclerView getDisplayView() {
        return display;
    }

    /**
     * Getter method for the button
     * @return Button for "Next"
     */
    public DirectionsDisplayNextButton getNextBtn() {
        return nextBtn;
    }

    /**
     *
     * @param view
     */
    private void onSettingsClicked(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}