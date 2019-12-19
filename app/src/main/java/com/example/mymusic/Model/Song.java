package com.example.mymusic.Model;

import android.net.Uri;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String album;
    private Uri coverUri;
    private int duration;
    private String streamUri;

    public Song(int id, String title, String artist, String album, Uri coverUri, int duration, String streamUri) {
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

    public long getDuration() {
        return duration;
    }
    public String getDuratonString(){
        return toTime(duration);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getStreamUri() {
        return streamUri;
    }

    public void setStreamUri(String streamUri) {
        this.streamUri = streamUri;
    }
    private static String toTime(int time){
        int seconds = time/1000;
        int minutes = seconds/60;
        int hours = minutes /60;
        minutes %=60;
        seconds %= 60;
        if (hours!=0) return String.format("%02d:%02d:%02d",hours,minutes,seconds);
        else return String.format("%02d:%02d",minutes,seconds);
    }
}
