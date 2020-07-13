package com.example.mpplayer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;

import com.example.mpplayer.MediaPlayerService;

public abstract class MusicServiceFragment extends Fragment {
    public static final String TAG = "MusicServiceFragment";
    ServiceConnection serviceConnection;
    public MediaPlayerService musicService;
    Intent playIntent;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder)iBinder;
                musicService = binder.getService();
                MusicServiceFragment.this.onServiceConnected(musicService);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG,"onServiceDisconnected");
                MusicServiceFragment.this.onServiceDisconnected();
            }
        };
    }

    // this acts like a callback.
    // mug. yo function gets called after service connected
    // so it must be override
    public abstract void onServiceConnected(MediaPlayerService musicService);
    public abstract void onServiceDisconnected();

    @Override
    public void onStart() {
        super.onStart();
        playIntent = new Intent(getActivity(), MediaPlayerService.class);
        playIntent.setAction("");
        getActivity().bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(playIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onStop()");
        MusicServiceFragment.this.onServiceDisconnected();
        getActivity().stopService(playIntent);
        getActivity().unbindService(serviceConnection);

    }
}