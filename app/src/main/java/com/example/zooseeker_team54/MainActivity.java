package com.example.zooseeker_team54;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to represent the functionality of the MainActivity that is displayed on launch of our app
 */
public class MainActivity extends AppCompatActivity {

    public LocationTracker locationTracker;

    public RecyclerViewPresenter<LocItem> searchResultPresenter;
    public RecyclerViewPresenter<LocItem> plannedLocsPresenter;

    private TextView planSizeText;
    private AutoCompleteTextView searchBarText;

    private Button clearBtn;
    private Button planBtn;

    private ViewModel viewModel;

    /**
     * Text Watcher for search bar textview
     */
    private TextWatcher searchBarTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            showSearchResult(editable.toString());
        }
    };

    // TODO: figure what should happen if a plan is there but users modify the plan in main

    /**
     * Create the activity from a given savedInstanceState and initialize everything
     * @param savedInstanceState the saved instance from before
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utilities.loadOldZooJson(this);

        // prevents UI difficulties resulting from a rotated screen
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get view model from ViewModelProvider
        viewModel = new ViewModelProvider(this).get(ViewModel.class);

        locationTracker = new LocationTracker(this, false);
        // locationTracker.startMockingRoute(Utilities.getMockRoute());

        // Get search bar EditText and bind a text watcher to it
        searchBarText = this.findViewById(R.id.search_bar);
        searchBarText.addTextChangedListener(searchBarTextWatcher);

        // generate a list of exhibits from utilities and create the array adapter for autocomplete suggestions
        List<String> exhibitNames = viewModel.getAllExhibitNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, exhibitNames);
        searchBarText.setAdapter(adapter);

        searchResultPresenter = new RecyclerViewPresenterBuilder<LocItem>()
                .setAdapter(new SearchResultAdapter())
                .setRecyclerView(this.findViewById(R.id.search_results))
                .setItemOnClickListener(this::addPlannedLoc)
                .getRecyclerViewPresenter();

        plannedLocsPresenter = new RecyclerViewPresenterBuilder<LocItem>()
                .setAdapter(new PlannedLocsAdapter())
                .setRecyclerView(this.findViewById(R.id.planned_locs))
                .setItemOnDeleteListener(this::removePlannedLoc)
                .getRecyclerViewPresenter();

        // get all the planned live LocItems
        viewModel.getAllPlannedLive()
                .observe(this, plannedLocsPresenter::setItems);

        // Show the size of the plan
        planSizeText = this.findViewById(R.id.plan_size);
        updatePlanSizeText();

        // Set up clear button for planned locations
        this.clearBtn = this.findViewById(R.id.clear_btn);
        clearBtn.setOnClickListener(this::onClearBtnClicked);

        // Set up plan button to take us to the route activity
        this.planBtn = this.findViewById(R.id.plan_btn);
        planBtn.setOnClickListener(this::onPlanButtonClicked);

    }

    /**
     * Finds route from a given list of LocItems
     * @param plannedLocItems List of LocItems to find a route for
     * @return HashMap of the route to be displayed
     */
    public HashMap<String, List<LocEdge>> findRoute(List<LocItem> plannedLocItems) {
        Pair<HashMap<String, List<LocEdge>>, HashMap<String, Double>> pair = Utilities.findRoute(plannedLocItems);
        HashMap<String, List<LocEdge>> route = pair.first;
        HashMap<String, Double> distances = pair.second;

        for (Map.Entry<String, Double> entry : distances.entrySet()) {
            String location = entry.getKey();
            Double newDistance = entry.getValue();

            LocItem targetLocItem = viewModel.getLocItemById(location);
            viewModel.updateLocCurrentDist(targetLocItem, newDistance);
        }

        LocItem targetLocItem = viewModel.getLocItemById("entrance_exit_gate");
        viewModel.addPlannedLoc(targetLocItem);
        return route;
    }

    /**
     * Removes the given LocItem from the viewModel
     * @param locItem LocItem to be removed
     */
    public void removePlannedLoc(LocItem locItem) {
        viewModel.removePlannedLoc(locItem);
        updatePlanSizeText();
    }

    /**
     * Adds the given LocItem to the viewModel
     * @param locItem LocItem to be added
     */
    public void addPlannedLoc(LocItem locItem) {
        viewModel.addPlannedLoc(locItem);
        updatePlanSizeText();
    }

    /**
     * Updates the planSizeText by getting the planSize
     */
    private void updatePlanSizeText() {
        planSizeText.setText(String.format("Planned (%s)"
                , Integer.toString(viewModel.countPlannedExhibits())));
    }

    /**
     * Using our searchResultAdapter, we display the search results from the given query
     * @param query String query typed in by user
     */
    private void showSearchResult(String query) {
        searchResultPresenter.setItems(Utilities.findSearchResult(query, viewModel.getAll()));
    }

    /**
     * Function for when our clear button is clicked in MainActivity
     * @param view Passed in when "Clear" is clicked
     */
    private void onClearBtnClicked(View view) {
        viewModel.clearPlannedLocs();
        planSizeText.setText("Planned (0)");
    }


    /**
     * Function for when our plan button is clicked in MainActivity
     * @param view Passed in when "Plan" is clicked
     */
    private void onPlanButtonClicked(View view) {

        // show an alert if plan size is 0
        if (plannedLocsPresenter.getAdapter().getItemCount() == 0) {
            Utilities.showAlert(this, "Plan list is empty, can't create plan!");
            return;
        }

        HashMap<String, List<LocEdge>> directions = findRoute(plannedLocsPresenter.getAdapter().getItems());

        // launch ShowRouteActivity to display directions
        Intent intent = new Intent(this, ShowRouteActivity.class);
        intent.putExtra("route", directions);
        startActivity(intent);
    }
}
