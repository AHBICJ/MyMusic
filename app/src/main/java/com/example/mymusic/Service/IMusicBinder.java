package com.example.mymusic.Service;

import android.content.res.AssetFileDescriptor;

import com.example.mymusic.Model.Song;

public interface IMusicBinder {
    void play();
    void pause();
    void set(AssetFileDescriptor afd);
    void setAndPlay(Song song);
    void set(Song song);
    void setAndPlay(AssetFileDescriptor afd);
    void seekTo(int pos);
    boolean hasSong();
    boolean isPlaying();
    int getCurrentPosition();
    void setCallBack(IStateChangeCallBack callBack);
}
