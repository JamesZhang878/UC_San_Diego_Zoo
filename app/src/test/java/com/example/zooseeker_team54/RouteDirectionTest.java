package com.example.zooseeker_team54;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.Lifecycle;
import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// User Story 4 Unit Tests
@RunWith(AndroidJUnit4.class)
public class RouteDirectionTest {
    LocItemDao dao;
    LocDatabase testDb;
    Intent mainIntent, routeDirectionIntent;

    @Before
    public void setUp() {
        mainIntent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        routeDirectionIntent = new Intent(ApplicationProvider.getApplicationContext(), RouteDirectionActivity.class);
    }

    @Before
    public void resetDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        testDb = Room.inMemoryDatabaseBuilder(context, LocDatabase.class)
                .allowMainThreadQueries()
                .build();
        LocDatabase.injectTestDatabase(testDb);

        List<LocItem> todos = LocItem.loadJSON(context, "sample_node_info.json");
        dao = testDb.LocItemDao();
        dao.insertAll(todos);
    }

    /**
     * Pass the RouteDirectionActivity class a single exhibit's directions to display.
     */
    @Test
    public void singleExhibitTest() {

        // Launch MainActivity class
        ActivityScenario<MainActivity> mainActivityActivityScenario = ActivityScenario.launch(mainIntent);
        mainActivityActivityScenario.onActivity(activity -> {

            LocItem lions = dao.get("lions");

            List<LocItem> testItems = new ArrayList<>();
            testItems.add(lions);
            activity.plannedLocsPresenter.setItems(testItems);

            activity.addPlannedLoc(lions);

            RouteInfo routeInfo = activity.findRoute(activity.plannedLocsPresenter.getItems());
            routeDirectionIntent.putExtra("routeInfo", routeInfo);
            System.out.println(Utilities.findDirections(routeInfo, lions, true));
        });

        ActivityScenario<RouteDirectionActivity> routeDirectionActivityActivityScenario = ActivityScenario.launch(routeDirectionIntent);
        routeDirectionActivityActivityScenario.onActivity(activity -> {
            String expectedDirections = "[Proceed on 'Entrance Way' 10 meters towards 'Entrance Plaza' from 'Entrance and Exit Gate'.\n" +
                    ", Proceed on 'Reptile Road' 100 meters towards 'Alligators' from 'Entrance Plaza'.\n" +
                    ", Proceed on 'Sharp Teeth Shortcut' 200 meters towards 'Lions' from 'Alligators'.\n" +
                    "]";
            List<LocEdge> directions = activity.routeDirectionPresenter.getItems();
            assertEquals(expectedDirections, directions.toString());
        });

    }

    /**
     * Pass the RouteDirectionActivity a plan with more than one exhibit.
     * Should only display the directions for the first exhibit in the plan.
     */
    @Test
    public void multipleExhibitTest() {

        // Launch MainActivity class
        ActivityScenario<MainActivity> mainActivityActivityScenario = ActivityScenario.launch(mainIntent);
        mainActivityActivityScenario.onActivity(activity -> {

            LocItem lions = dao.get("lions");
            LocItem gators = dao.get("gators");

            List<LocItem> testItems = new ArrayList<>();
            testItems.add(lions);
            testItems.add(gators);
            activity.plannedLocsPresenter.setItems(testItems);

            activity.addPlannedLoc(lions);
            activity.addPlannedLoc(gators);

            RouteInfo routeInfo = activity.findRoute(activity.plannedLocsPresenter.getItems());
            routeDirectionIntent.putExtra("routeInfo", routeInfo);
        });

        ActivityScenario<RouteDirectionActivity> routeDirectionActivityActivityScenario = ActivityScenario.launch(routeDirectionIntent);
        routeDirectionActivityActivityScenario.onActivity(activity -> {
            String expectedDirections = "[Proceed on 'Entrance Way' 10 meters towards 'Entrance Plaza' from 'Entrance and Exit Gate'.\n" +
                    ", Proceed on 'Reptile Road' 100 meters towards 'Alligators' from 'Entrance Plaza'.\n" +
                    "]";
            List<LocEdge> directions = activity.routeDirectionPresenter.getItems();
            assertEquals(expectedDirections, directions.toString());
        });
    }
}
