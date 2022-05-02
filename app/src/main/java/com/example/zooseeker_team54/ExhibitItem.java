package com.example.zooseeker_team54;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@Entity(tableName = "exhibit_items")
public class ExhibitItem {

     @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;
    public boolean planned;

    @NonNull
    @Override
    public String toString() {
        return "Exhibit{" +
                "id=" + id +
                ", name='" + name +
                '}';
    }

    ExhibitItem(@NonNull String name, boolean planned) {
        this.name = name;
        this.planned = planned;
    }

    public static List<ExhibitItem> loadJSON(Context context, String path) {
        try {
            InputStream input = context.getAssets().open(path);
            Reader reader = new InputStreamReader(input);
            Gson gson = new Gson();
            Type type = new TypeToken<List<ExhibitItem>>(){}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
