package com.example.mpplayer.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.mpplayer.MediaPlayerService;
import com.example.mpplayer.R;
import com.example.mpplayer.customlisteners.AudioListsProvider;
import com.example.mpplayer.model.Album;
import com.example.mpplayer.model.Artist;
import com.example.mpplayer.model.Audio;
import com.example.mpplayer.utils.StorageUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MediaPlayerService.MediaChangeListener, AudioListsProvider {

    SeekBar seekBar;
    ImageButton like, notlike, dislike, notdislike;
    ImageButton play, pause, play_main, pause_main;
    ImageButton slidingUpPreviousBtn, slidingUpNextBtn, slidingUpPlayBtn, slidingUpPauseBtn;
    private SlidingUpPanelLayout slidingUpPanelLayout;

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.mpplayer.PlayNewAudio";
    public static final String TAG = MainActivity.class.getSimpleName();
    public static int REQUEST_PERMISSION = 123;
    public boolean hasPermission = false;
    ImageView collapsingImageView;

    private MediaPlayerService player;
    boolean serviceBound = false;


    private ArrayList<Audio> audioList;
    private ArrayList<Artist> artistList;
    private ArrayList<Album> albumList;

    TextView songTitle, songArtistName;
    TextView endTime, currentTime;
    ImageView trackImage;
    private StorageUtil storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        trackImage = findViewById(R.id.track_image);

        notlike = findViewById(R.id.imageButton2new);
        notdislike = findViewById(R.id.buttontwo);
        play = findViewById(R.id.play_button);
        pause = findViewById(R.id.pause_button);
        play_main = findViewById(R.id.play_button_main);
        pause_main = findViewById(R.id.pause_button_main);


        slidingUpPanelLayout = findViewById(R.id.home_screen_main);
        slidingUpPreviousBtn = (ImageButton) findViewById(R.id.sliding_up_previous);
        slidingUpNextBtn = (ImageButton) findViewById(R.id.sliding_up_next);


        songTitle = findViewById(R.id.songs_title);
        songArtistName = findViewById(R.id.songs_artist_name);

        endTime = (TextView) findViewById(R.id.endTime);
        currentTime = (TextView) findViewById(R.id.currentTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        storage = new StorageUtil(getApplicationContext());
//        seekBarThread = new SeekBarThread();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBound) {
                    onSongPlay();
                    Intent broadcastIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                    broadcastIntent.setAction(MediaPlayerService.ACTION_PLAY);
                    startService(broadcastIntent);
                }

            }
        });


        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBound) {
                    onSongPause();
                    Intent broadcastIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                    broadcastIntent.setAction(MediaPlayerService.ACTION_PAUSE);
                    startService(broadcastIntent);
                }
            }
        });

        slidingUpPreviousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBound) {
                    Intent broadcastIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                    broadcastIntent.setAction(MediaPlayerService.ACTION_PREVIOUS);
                    startService(broadcastIntent);
                }

            }
        });


        slidingUpNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBound) {
                    Intent broadcastIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                    broadcastIntent.setAction(MediaPlayerService.ACTION_NEXT);
                    player.handleIncomingActions(broadcastIntent);
                }

            }
        });
        play_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBound) {
                    onSongPlay();
                    Intent broadcastIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                    broadcastIntent.setAction(MediaPlayerService.ACTION_PLAY);
                    startService(broadcastIntent);
                }

            }
        });

        pause_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serviceBound) {
                    onSongPause();
                    Intent broadcastIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                    broadcastIntent.setAction(MediaPlayerService.ACTION_PAUSE);
                    startService(broadcastIntent);
                }

            }
        });

        checkPermission();


        if (!hasPermission) {
            Log.d("aaa", "onCreate: " + hasPermission);
        }
        loadAudio();


        //    Log.d("song", "onCreate: song" + audioList.get(0).getData());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    player.seekTo(seekBar.getProgress());
                } catch (Exception ex) {

                }
            }
        });


    }


    public String toTimeFormat(long millSecond) {
        long duration = millSecond / 1000;
        int hours = (int) duration / 3600;
        int remainder = (int) duration - hours * 3600;
        int minute = remainder / 60;
        remainder = remainder - minute * 60;
        int second = remainder;
        String strMinute = Integer.toString(minute);
        String strSecond = Integer.toString(second);
        String strHour;
        if (strMinute.length() < 2) {
            strMinute = "0" + minute;
        }
        if (strSecond.length() < 2) {
            strSecond = "0" + second;
        }
        if (hours == 0) {
            return strMinute + ":" + strSecond;
        } else {
            return hours + ":" + strMinute + ":" + strSecond;
        }
    }


    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout != null &&
                (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
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


    // Binding this client to the audioplayer service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // we've bound to LocalService , cast the IBinder and get LocalService instance
//            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
            player.startMediaActionListener(MainActivity.this);
            new SeekBarAsync().execute();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

//            Toast.makeText(MainActivity.this, "Service Not Bound", Toast.LENGTH_SHORT).show();
            serviceBound = false;
        }
    };

    //Set Song from recyclerview's items
    private void playAudio(int audioIndex) {
        // check if service is active
        storage.storeAudio(audioList);
        storage.storeAudioIndex(audioIndex);

        if (!serviceBound) {
            // store serializable audiolist to sharedPreferences
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", audioIndex);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(playerIntent);
            } else
                startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // store the new audioIndex to sharedPreferences
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
        audioList = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Audio audio = new Audio(data, title, album, artist);
                audioList.add(audio);
            }
        }


        String[] mProjection =
                {
                        MediaStore.Audio.Artists._ID,
                        MediaStore.Audio.Artists.ARTIST,
                        MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                        MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
                };


        cursor = contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                mProjection,
                null,
                null,
                MediaStore.Audio.Artists.ARTIST + " ASC");


        artistList = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String artistId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists._ID));
                String albumCount = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS));
                String trackCount = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
                artistList.add(new Artist(artistId, albumCount, trackCount, artist));
            }
        }


        String[] projectionAlbum =
                {
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.ARTIST,
                        MediaStore.Audio.Albums.ALBUM,
                        MediaStore.Audio.Albums.ALBUM_ART,
                        MediaStore.Audio.Albums.NUMBER_OF_SONGS
                };


        cursor = contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projectionAlbum,
                null,
                null,
                MediaStore.Audio.Albums.ALBUM + " ASC");


        albumList = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                String albumArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                String trackCount = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS));
                albumList.add(new Album(albumId, artist, album, albumArt, trackCount));
            }
        }
        System.out.println(artistList.size());
        System.out.println(albumList.size());

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

        if (serviceBound) {
            unbindService(serviceConnection);
            // service is active
            player.stopSelf();
        }
        super.onDestroy();

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


    @Override
    public void onSongNext() {
        Audio audio = audioList.get(storage.loadAudioIndex());
        songArtistName.setText(audio.getArtist());
        songTitle.setText(audio.getTitle());

    }

    @Override
    public void onSongPause() {
        play_main.setVisibility(View.VISIBLE);
        play.setVisibility(View.VISIBLE);
        pause_main.setVisibility(View.GONE);
        pause.setVisibility(View.GONE);
    }

    @Override
    public void onSongPrevious() {
        Audio audio = audioList.get(storage.loadAudioIndex());
        songArtistName.setText(audio.getArtist());
        songTitle.setText(audio.getTitle());
    }

    @Override
    public void onSongPlay() {
        play_main.setVisibility(View.GONE);
        play.setVisibility(View.GONE);
        pause_main.setVisibility(View.VISIBLE);
        pause.setVisibility(View.VISIBLE);
    }

    public void onTrackSelected(int index) {
        songTitle.setText(audioList.get(index).getTitle());
        songArtistName.setText(audioList.get(index).getArtist());
        playAudio(index);
        onSongPlay();
        if (getAlbumImage(audioList.get(index).getData()) != null)
            trackImage.setImageBitmap(getAlbumImage(audioList.get(index).getData()));
        else
            trackImage.setImageDrawable(getResources().getDrawable(R.drawable.image1));
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

    }

    public void setNowPlaying(ArrayList<Audio> audioList, int position) {
        this.audioList = audioList;
        onTrackSelected(position);

    }


    private class SeekBarAsync extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected Integer doInBackground(Void... voids) {
            while (serviceBound) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final int currentTimeInMs = player.getCurrentMediaPosition();
                publishProgress(currentTimeInMs);
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            currentTime.setText(toTimeFormat(values[0]));
            seekBar.setProgress(values[0]);
            int endTimeInMs = player.getSongTimeDuration();
            seekBar.setMax(endTimeInMs);
            endTime.setText(toTimeFormat(endTimeInMs));
        }
    }

    private Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
        return null;
    }


    @Override
    public ArrayList<Audio> getAudioList() {
        return audioList;
    }

    @Override
    public ArrayList<Artist> getArtistList() {
        return artistList;
    }

    @Override
    public ArrayList<Album> getAlbumList() {
        return albumList;
    }


}
