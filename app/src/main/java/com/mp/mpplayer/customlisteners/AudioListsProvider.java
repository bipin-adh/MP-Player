package com.mp.mpplayer.customlisteners;

import com.mp.mpplayer.model.Album;
import com.mp.mpplayer.model.Artist;
import com.mp.mpplayer.model.Audio;

import java.util.ArrayList;

public interface AudioListsProvider {
    ArrayList<Audio> getAudioList();

    ArrayList<Artist> getArtistList();

    ArrayList<Album> getAlbumList();
}
