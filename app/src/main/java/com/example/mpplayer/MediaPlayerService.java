package com.example.mpplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.mpplayer.activities.MainActivity;
import com.example.mpplayer.model.Audio;
import com.example.mpplayer.utils.StorageUtil;

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener
        , MediaPlayer.OnPreparedListener
        , MediaPlayer.OnErrorListener
        , MediaPlayer.OnSeekCompleteListener
        , MediaPlayer.OnInfoListener
        , MediaPlayer.OnBufferingUpdateListener
        , AudioManager.OnAudioFocusChangeListener {


    public static final String TAG = "MediaPlayer Error";
    //Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    private MediaPlayer mediaPlayer;

    // path to the audio file
    private String mediaFile;

    //Used to pause/resume MediaPlayer
    private int resumePosition;

    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;


    // list of available audio files
    /**
     * The audio is not passed to the Service through putExtra(),
     * so the Service has to load the data from the SharedPreferences
     * and this is why the onStartCommand() method needs to be rewritten.
     */
    private ArrayList<Audio> audioList;
    private int audioIndex = -1;
    private Audio activeAudio; //an object of the currently playing audio

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        // set up media player event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        // reset so that MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // set the data source to the mediaFile location
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }


    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // Invoked when playback of a media source has completed.
        stopMedia();
        //stop the service
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //  Invoked when the media source is ready for playback.
        playMedia();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.

        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d(TAG, "Media error not valid for progressive playback " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d(TAG, "Media error server died " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d(TAG, "Media error unknown" + extra);
                break;
        }
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of a media resource being streamed over the network.
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        /**
         * AudioManager.AUDIOFOCUS_GAIN – The service gained audio focus, so it needs to start playing.
         * AudioManager.AUDIOFOCUS_LOSS – The service lost audio focus,
         * the user probably moved to playing media on another app, so release the media player.
         * AudioManager.AUDIOFOCUS_LOSS_TRANSIENT – Focus lost for a short time, pause the MediaPlayer.
         * AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK – Lost focus for a short time,
         * probably a notification arrived on the device, lower the playback volume.
         */

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //resume playback
                if (mediaPlayer == null) {
                    initMediaPlayer();
                } else if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // lost focus for an unbounded amount of time. so stop playback and release mediaplayer
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // focus gained
            return true;
        }
        // could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {

        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);

    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    /**
     * The onStartCommand() handles the initialization of the MediaPlayer and the focus request to make sure
     * there are no other apps playing media. In the onStartCommand() code
     * I added an extra try-catch block to make sure the getExtras() method doesn’t throw a NullPointerException
     */
    // The system calls this method when an activity requests the service to be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            // an audio file is pased to the service through putExtra();
            mediaFile = intent.getExtras().getString("media");
        } catch (NullPointerException e) {
            stopSelf();
        }

        // request audio focus

        if (requestAudioFocus() == false) {
            // could not gain focus
            stopSelf();
        }
        if (mediaFile != null && mediaFile != "") {
            initMediaPlayer();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Another important method you need to implement is onDestroy().
     * In this method the MediaPlayer resources must be released,
     * as this service is about to be destroyed and there is no need for the app to control the media resources.
     * You must unregister all the registered BroadcastReceivers when they are not needed anymore.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        //clear cached playlist
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    /**
     * In media apps it’s common that when a user removes their headphones from the jack the media stops playing.
     * <p>
     * A BroadcastReceiver that listens to ACTION_AUDIO_BECOMING_NOISY,
     * which means that the audio is about to become ‘noisy’ due to a change in audio outputs.
     * Add the following functions helps on that issue.
     */
    // Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        // register after getting audio focus
        /**
         * The BroadcastReceiver instance will pause the MediaPlayer when the system makes an ACTION_AUDIO_BECOMING_NOISY call.
         * To make the BroadcastReceiver available you must register it. The registerBecomingNoisyReceiver() function
         * handles this and specifies the intent action BECOMING_NOISY which will trigger this BroadcastReceiver.
         */
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    /**
     * When the MediaPlayerService is playing something and the user wants to play a new track,
     * you must notify the service that it needs to move to new audio.
     * You need a way for the Service to listen to these “play new Audio” calls and act on them. How? Another BroadcastReceiver.
     * We mentioned these “play new Audio” calls in the Redefine methods section when calling the playAudio() function.
     */
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // get the new media index from the sharedPreferences
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioList.size() > 0) {
                // index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };
    private void register_playNewAudio(){
        // register playNewMedia receiver
        /**
         * When intercepting a PLAY_NEW_AUDIO intent,
         * this BroadcastReceiver loads the updated index and
         * updates the activeAudio object to the new media and the MediaPlayer is reset to play the new audio.
         */
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio,filter);
    }

    /**
     * Handle PhoneState changes
     * <p>
     * The callStateListener() function is an implementation of the PhoneStateListener
     * that listens to TelephonyManagers state changes.
     * TelephonyManager provides access to information about the telephony services on the device
     * and listens for changes to the device call state and reacts to these changes.
     */
    private void callStateListener() {
        // get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // starting listening for phoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
}
