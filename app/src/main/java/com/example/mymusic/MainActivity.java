package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusic.Adapter.SongAdapter;
import com.example.mymusic.Model.Song;
import com.example.mymusic.Utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SeekBar seekBar;
    private TextView current;
    private TextView slash;
    private TextView duration;
    private TextView title;
    private ImageButton play;
    private ImageButton pre;
    private ImageButton next;
    private SongAdapter mAdapter;
    private ArrayList<Song> songs;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        getSongList();
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mAdapter = new SongAdapter(getApplicationContext(), songs, (song, position) -> {
            changeSong(song,position);
        });
        recyclerView.setAdapter(mAdapter);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(mp -> {
            togglePlay(mp);
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            int pos = mAdapter.getSelectedPosition()+1 % mAdapter.getItemCount();
            changeSong(songs.get(pos),pos);
        });

        handleSeekbar();
    }

    private void togglePlay(MediaPlayer mp) {
        if (mp.isPlaying()){
            mp.stop();
            mp.reset();
        }else{
            mp.start();
            play.setBackgroundResource(R.drawable.pause);
            final Handler mHandler = new Handler();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                    current.setText(Utils.intToTime(mediaPlayer.getCurrentPosition()));
                    mHandler.postDelayed(this,1000);
                }
            });
        }
    }

    private void initializeViews() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        current = (TextView) findViewById(R.id.current);
        slash = (TextView) findViewById(R.id.slash);
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
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                Uri streamUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);
                songs.add(new Song(id, title, artist, album, albumArtUri, duration, streamUri));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void handleSeekbar(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer!=null && fromUser){
                    current.setText(Utils.intToTime(progress*1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer!=null){
                    mediaPlayer.seekTo(seekBar.getProgress()*1000);
                }
            }
        });
    }

    private void changeSong(Song song,int position){
        if (position == mAdapter.getSelectedPosition() && mediaPlayer.isPlaying()) return;
        mAdapter.setSelectedPosition(position);
        ContentResolver resolver = getApplicationContext().getContentResolver();
        String readOnlyMode = "r";
        try(AssetFileDescriptor parcelFd = resolver.openAssetFileDescriptor(song.getStreamUri(), readOnlyMode)){
            duration.setText(song.getDuratonString());
            title.setText(song.getTitle());
            seekBar.setMax(song.getDuration()/1000);
            playSong(parcelFd);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void playSong(AssetFileDescriptor parcelFd) {
        mediaPlayer.reset();
        try{
            mediaPlayer.setDataSource(parcelFd);
            mediaPlayer.prepareAsync();
        }catch (IOException e){
            e.printStackTrace();
        };
    }
}
