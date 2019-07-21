package com.example.mpplayer.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mpplayer.model.Audio;
import com.example.mpplayer.customlisteners.CustomTouchListener;
import com.example.mpplayer.MediaPlayerService;
import com.example.mpplayer.R;
import com.example.mpplayer.adapters.RecyclerViewAdapter;
import com.example.mpplayer.customlisteners.onItemClickListener;
import com.example.mpplayer.utils.StorageUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.mpplayer.PlayNewAudio";
    public static final String TAG = MainActivity.class.getSimpleName();
    public static int REQUEST_PERMISSION = 123;
    public boolean hasPermission = false;
    ImageView collapsingImageView;

    private MediaPlayerService player;
    boolean serviceBound = false;
    int imageIndex = 0;

    // to store local media files in a list
    ArrayList<Audio> audioList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        collapsingImageView = (ImageView) findViewById(R.id.collapsingImageView);
        checkPermission();

        loadCollapsingImage(imageIndex);

        if (!hasPermission) {
            Log.d("aaa", "onCreate: " + hasPermission);
        }
        loadAudio();
        initRecyclerView();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
                //play the first audio in the ArrayList
//                playAudio(2);
                if (imageIndex == 4) {
                    imageIndex = 0;
                    loadCollapsingImage(imageIndex);
                } else {
                    loadCollapsingImage(++imageIndex);
                }
            }
        });
        Log.d("song", "onCreate: song" + audioList.get(0).getData());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void loadCollapsingImage(int i) {
        TypedArray array = getResources().obtainTypedArray(R.array.images);
        collapsingImageView.setImageDrawable(array.getDrawable(i));
    }

    private void initRecyclerView(){
        if(audioList.size()>0){
            RecyclerView recyclerView = findViewById(R.id.recyclerview);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(audioList,getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            recyclerView.addOnItemTouchListener(new CustomTouchListener(this, new onItemClickListener() {
                @Override
                public void onClick(View view, int index) {
                    playAudio(index);
                }
            }));
        }

    }
    // Binding this client to the audioplayer service
    private ServiceConnection serviceConnection = new ServiceConnection() {
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
    private void playAudio(int audioIndex) {
        // check if service is active
        if (!serviceBound) {
            // store serializable audiolist to sharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", audioIndex);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // store the new audioIndex to sharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);
            // service is active
            // send media with broadcast receiver
            // send a broadcast to the service --> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
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
