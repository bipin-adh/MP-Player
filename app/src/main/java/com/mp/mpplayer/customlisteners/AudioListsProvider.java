package com.example.mpplayer.customlisteners;

import com.example.mpplayer.model.Album;
import com.example.mpplayer.model.Artist;
import com.example.mpplayer.model.Audio;

import java.util.ArrayList;

public interface AudioListsProvider {
    ArrayList<Audio> getAudioList();
    ArrayList<Artist> getArtistList();
    ArrayList<Album> getAlbumList();
}
