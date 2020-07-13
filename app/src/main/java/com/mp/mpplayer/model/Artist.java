package com.mp.mpplayer.model;

import java.io.Serializable;

public class Artist implements Serializable {
    String artistId;
    String albumCount;
    String trackCount;
    String Artist;

    public Artist(String artistId, String albumCount, String trackCount, String artist) {
        this.artistId = artistId;
        this.albumCount = albumCount;
        this.trackCount = trackCount;
        Artist = artist;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getAlbumCount() {
        return albumCount;
    }

    public void setAlbumCount(String albumCount) {
        this.albumCount = albumCount;
    }

    public String getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(String trackCount) {
        this.trackCount = trackCount;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }
}
