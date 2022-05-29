package com.example.zooseeker_team54;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.List;


/**
 * Activity to show the route of exhibits on our plan. Launched when "Plan" is clicked
 * on MainActivity
 */
public class ShowRouteActivity extends AppCompatActivity {

    private ViewModel viewModel;
    public RecyclerViewPresenter<LocItem> showRoutePresenter;

    private RouteInfo routeInfo;

    private Button directionBtn;
    private Button backBtn;

    /**
     * Create the activity from a given savedInstanceState and initialize everything
     *
     * @param savedInstanceState the saved instance from before
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_plan);
        Intent intent = getIntent();
        routeInfo = (RouteInfo) intent.getSerializableExtra("routeInfo");

        viewModel = new ViewModelProvider(this).get(ViewModel.class);

        showRoutePresenter = new RecyclerViewPresenterBuilder<LocItem>()
                .setAdapter(new ShowRouteAdapter(routeInfo))
                .setRecyclerView(this.findViewById(R.id.planned_route))
                .getRecyclerViewPresenter();

        viewModel.getAllPlannedUnvisitedLive()
                .observe(this, new Observer<List<LocItem>>() {
                    @Override
                    public void onChanged(List<LocItem> locItems) {

                    }
                });

        directionBtn = findViewById(R.id.direction_btn);
        directionBtn.setOnClickListener(this::onDirectionBtnClicked);

        backBtn = findViewById(R.id.go_back_btn);
        backBtn.setOnClickListener(this::onBackButtonClicked);
    }

    /**
     * Back out of the activity when the "Back" button is clicked
     *
     * @param view View that's passed in
     */
    private void onBackButtonClicked(View view) {
        finish();
    }

    /**
     * Function for when the Direction button is clicked, and launches the RouteDirectionActivity
     *
     * @param view
     */
    private void onDirectionBtnClicked(View view) {

        // user selection of brief display vs detailed display
        String target = routeInfo.getNextUnvisitedExhibit();
        Intent intent = new Intent(this, ShowDirectionActivity.class);

        // show an alert if target doesn't exist or is null
        if (target == null) {
            String alertMessage = "All exhibits visited! " + "" +
                    "Please clear all selections on the previous page " +
                    "with the CLEAR button " +
                    "and select more exhibits to visit.";
            Utilities.showAlert(this, alertMessage);
            return;
        }

        //
        intent.putExtra("routeInfo", routeInfo);
        startActivity(intent);
    }
}

