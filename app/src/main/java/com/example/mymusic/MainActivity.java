package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mymusic.Adapter.SongAdapter;
import com.example.mymusic.Model.Song;
import com.example.mymusic.Service.IMusicBinder;
import com.example.mymusic.Service.MusicService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SeekBar seekBar;
    private TextView current;
    private TextView duration;
    private TextView title;
    private ImageButton play;
    private ImageButton pre;
    private ImageButton next;
    private SongAdapter mAdapter;
    private ArrayList<Song> songs;
    private IMusicBinder myService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        Intent intent = new Intent(this, MusicService.class);
        MyConnection myConnection = new MyConnection();
        bindService(intent,myConnection,BIND_AUTO_CREATE);

        getSongList();
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        HanderButtonListener mButtonListener = new HanderButtonListener();
        play.setOnClickListener(mButtonListener);
        pre.setOnClickListener(mButtonListener);
        next.setOnClickListener(mButtonListener);

        mAdapter = new SongAdapter(getApplicationContext(), songs, (song, position) -> {
            myService.setAndPlay(song);
        });

        recyclerView.setAdapter(mAdapter);

//        handleSeekbar();
    }

    private class MyConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = (IMusicBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private void initializeViews() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        current = (TextView) findViewById(R.id.current);
        duration = (TextView) findViewById(R.id.duration);
        title = (TextView) findViewById(R.id.title);
        play = (ImageButton) findViewById(R.id.play);
        pre = (ImageButton) findViewById(R.id.pre);
        next = (ImageButton) findViewById(R.id.next);
    }

    private void getSongList() {
        songs = new ArrayList<>();

        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        final String[] cursor_cols = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final Cursor cursor = getApplicationContext().getContentResolver().query(uri,
                cursor_cols, where, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                if (duration < 30000) continue;
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                Uri streamUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);
                songs.add(new Song(id, title, artist, album, albumArtUri, duration, streamUri));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

//    private void handleSeekbar(){
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (mediaPlayer!=null && fromUser){
//                    current.setText(Utils.intToTime(progress*1000));
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                if (mediaPlayer!=null){
//                    mediaPlayer.seekTo(seekBar.getProgress()*1000);
//                }
//            }
//        });
//    }

    private class HanderButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.pre:
                    break;
                case R.id.play:
                    if (myService.isPlaying()) {
                        myService.pause();
                    }else {
                        myService.play();
                    }
                    break;
                case R.id.next:
                    break;
            }
        }
    }
}
