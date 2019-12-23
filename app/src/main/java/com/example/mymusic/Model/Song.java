package com.example.mymusic.Model;

import android.net.Uri;

import com.example.mymusic.Utils.Utils;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String album;
    private Uri coverUri;
    private int duration;
    private Uri streamUri;

    public Song(int id, String title, String artist, String album, Uri coverUri, int duration, Uri streamUri) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.coverUri = coverUri;
        this.duration = duration;
        this.streamUri = streamUri;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Uri getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(Uri coverUri) {
        this.coverUri = coverUri;
    }

    public int getDuration() {
        return duration;
    }
    public String getDuratonString(){
        return Utils.intToTime(duration);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Uri getStreamUri() {
        return streamUri;
    }

    public void setStreamUri(Uri streamUri) {
        this.streamUri = streamUri;
    }

}
