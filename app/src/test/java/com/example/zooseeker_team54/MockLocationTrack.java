package com.example.zooseeker_team54;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.checkerframework.checker.units.qual.C;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MockLocationTrack {
    LocItemDao dao;
    LocDatabase testDb;
    Intent mainIntent, routeDirectionIntent;

    @Before
    public void setUp() {
        mainIntent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        routeDirectionIntent = new Intent(ApplicationProvider.getApplicationContext(), ShowDirectionActivity.class);
    }


    @Before
    public void resetDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        testDb = Room.inMemoryDatabaseBuilder(context, LocDatabase.class)
                .allowMainThreadQueries()
                .build();
        LocDatabase.injectTestDatabase(testDb);

        List<LocItem> todos = LocItem.loadJSON(context, "exhibit_info.json");
        dao = testDb.LocItemDao();
        dao.insertAll(todos);
    }

    @Test
    public void checkDefaultCoord() {

        ActivityScenario<ShowDirectionActivity> routeDirectionActivityActivityScenario = ActivityScenario.launch(routeDirectionIntent);
        routeDirectionActivityActivityScenario.onActivity(activity -> {
            Coord coord = activity.getCoord();
            assertEquals(coord, new Coord(32.73459618734685, -117.14936));
        });

    }

    @Test
    public void goOffRouteWithTheSameTarget() {

        ActivityScenario<MainActivity> mActivityActivityScenario = ActivityScenario.launch(mainIntent);
        mActivityActivityScenario.onActivity(activity -> {

            LocItem fern_canyon = dao.get("fern_canyon");
            LocItem gorilla = dao.get("gorilla");

            List<LocItem> testItems = new ArrayList<>();
            testItems.add(fern_canyon);
            testItems.add(gorilla);
            activity.getPlannedLocsPresenter().setItems(testItems);

            RouteInfo routeInfo = activity.findRoute(activity.getPlannedLocsPresenter().getItems());
            routeDirectionIntent.putExtra("routeInfo", routeInfo);
        });

        ActivityScenario<ShowDirectionActivity> routeDirectionActivityActivityScenario = ActivityScenario.launch(routeDirectionIntent);
        routeDirectionActivityActivityScenario.onActivity(activity -> {
            RouteInfo routeInfo = activity.getRouteInfo();
            System.out.println(routeInfo.getDirections());
            List<LocEdge> oldRouteForCanyon = routeInfo.getDirection("fern_canyon");
            LocationTracker locationTracker = activity.getLocationTracker();
            LocItem siamang = dao.get("siamang");
            locationTracker.mockLocation(siamang.getCoord());
            List<LocEdge> newRouteForCanyon = routeInfo.getDirection("fern_canyon");
            assertNotEquals(oldRouteForCanyon, newRouteForCanyon);
        });


    }
}
