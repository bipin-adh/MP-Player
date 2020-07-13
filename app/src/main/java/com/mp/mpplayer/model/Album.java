package com.mp.mpplayer.model;

import java.io.Serializable;

public class Album implements Serializable {

    String albumId;
    String artist;
    String album;
    String albumArt;
    String trackCount;

    public Album(String albumId, String artist, String album, String albumArt, String trackCount) {
        this.albumId = albumId;
        this.artist = artist;
        this.album = album;
        this.albumArt = albumArt;
        this.trackCount = trackCount;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(String trackCount) {
        this.trackCount = trackCount;
    }
}
