package com.example.mpplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static int REQUEST_PERMISSION = 123;
    public boolean hasPermission = false;

    private MediaPlayerService player;
    boolean serviceBound = false;
    private ServiceConnection serviceConnection;

    // to store local media files in a list
    ArrayList<Audio> audioList;

    private void playAudio(String media) {
        // check if service is active
        if (serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // service is active
            // send media with broadcast receiver
        }
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {

                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Audio audio = new Audio(data, title, album, artist);

                // save to audiolist
                audioList.add(audio);
            }
        }
        cursor.close();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        if (!hasPermission) {
            Log.d("aaa", "onCreate: " + hasPermission);
        }
        loadAudio();
        Log.d("song", "onCreate: song" + audioList.get(0).getData());
        playAudio(audioList.get(0).getData());
        // Binding this client to the audioplayer service
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // we've bound to LocalService , cast the IBinder and get LocalService instance
                Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
                MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
                player = binder.getService();
                serviceBound = true;


            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

                Toast.makeText(MainActivity.this, "Service Not Bound", Toast.LENGTH_SHORT).show();
                serviceBound = false;
            }
        };
    }


    /**
     * onSaveInstanceState() , onRestoreInstanceState() , onDestroy()
     * If you call the playAudio() function from the Activitys onCreate() method
     * the Service will start playing, but the app can easily crash.
     * <p>
     * The following methods to MainActivity to fix it.
     * All these methods do is save and restore the state of the serviceBound variable and
     * unbind the Service when a user closes the app.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serviceBound) {
            unbindService(serviceConnection);
            // service is active
            player.stopSelf();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode + hasPermission);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
                Log.d(TAG, "onRequestPermissionsResult: " + requestCode + hasPermission);
            } else {
                Log.d(TAG, "onRequestPermissionsResult: " + requestCode + hasPermission);
                finish();
            }
        }
    }

    public void checkPermission() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "checkPermission: " + hasPermission);
                hasPermission = true;
            } else {
                Log.d(TAG, "checkPermission: " + hasPermission);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                hasPermission = false;
            }
        } else {
            Log.d(TAG, "checkPermission: " + hasPermission);
            hasPermission = true;
        }
    }
}
