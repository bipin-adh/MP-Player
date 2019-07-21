package com.example.mpplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mpplayer.model.Audio;
import com.google.gson.Gson;

import java.util.ArrayList;

public class StorageUtil {

    private final String STORAGE = "com.example.mpplayer.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<Audio> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1); // return -1 if no data found
    }

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
