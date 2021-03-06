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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mymusic.Adapter.SongAdapter;
import com.example.mymusic.Model.Song;
import com.example.mymusic.Service.IMusicBinder;
import com.example.mymusic.Service.IStateChangeCallBack;
import com.example.mymusic.Service.MusicService;
import com.example.mymusic.Utils.Utils;

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
    private boolean isDragging;
    private final Handler mHandler = new Handler();
    MyConnection myConnection;
    boolean mBound = false;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isDragging){
                seekBar.setProgress(myService.getCurrentPosition()/1000);
                current.setText(Utils.intToTime(myService.getCurrentPosition()));
            }
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        Intent intent = new Intent(this, MusicService.class);
        myConnection = new MyConnection();
        bindService(intent,myConnection,BIND_AUTO_CREATE);
        mBound = true;

        // 获得歌曲列表
        getSongList();
        // 设置线性布局
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        // 设置分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        // 传入一个回调函数实例化一个适配器
        mAdapter = new SongAdapter(getApplicationContext(), songs, (song, position) -> {
            playSong(position);
        });
        // 设置适配器
        recyclerView.setAdapter(mAdapter);

        HandlerButtonListener mButtonListener = new HandlerButtonListener();
        play.setOnClickListener(mButtonListener);
        pre.setOnClickListener(mButtonListener);
        next.setOnClickListener(mButtonListener);

        handleSeekbar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(myConnection);
        mBound = false;
    }


    private class MyConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = (IMusicBinder) service;
            myService.setCallBack(new StateChangeCallBack());
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
        // 得到音频媒体的URI
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // 定义需要选择的字段
        final String[] cursor_cols = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};
        // 定义筛选，首先可以通过系统的字段 IS_MUSIC过滤掉一部分非音乐
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        // 获得通过ContentResolver 查询对应的内容提供器
        final Cursor cursor = getApplicationContext().getContentResolver().query(uri,
                cursor_cols, where, null, null);
        // 对获得的游标 进行处理，生成歌曲对象放入歌曲列表
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
                // 获得专辑封面对应的 URI
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                // 获得歌曲对应的 URI
                Uri streamUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);
                songs.add(new Song(id, title, artist, album, albumArtUri, duration, streamUri));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void handleSeekbar(){
        isDragging = false;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (myService.hasSong() && fromUser){
                    current.setText(Utils.intToTime(progress*1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDragging=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isDragging=false;
                myService.seekTo(seekBar.getProgress()*1000);
            }
        });
    }

    private void playSong(int pos) {
        mAdapter.setSelectedPosition(pos);
        Song song = songs.get(pos);
        myService.set(song);
        title.setText(song.getTitle());
        seekBar.setMax(song.getDuration()/1000);
        seekBar.setProgress(0);
        duration.setText(song.getDuratonString());
        resume();
    }

    private void pause(){
        myService.pause();
        play.setBackgroundResource(R.drawable.play);
        stopRefreshSeekBar();
    }

    private void resume(){
        myService.play();
        play.setBackgroundResource(R.drawable.pause);
        startRefreshSeekBar();
    }

    private void startRefreshSeekBar(){
        mRunnable.run();
    }

    private void stopRefreshSeekBar(){
        mHandler.removeCallbacks(mRunnable);
    }

    private class HandlerButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            int pos;
            switch (v.getId()){
                case R.id.pre:
                    pos = (mAdapter.getSelectedPosition()-1+mAdapter.getItemCount())%mAdapter.getItemCount();
                    playSong(pos);
                    break;
                case R.id.play:
                    if (!myService.hasSong()) break;
                    if (myService.isPlaying()) {
                        pause();
                    }else {
                        resume();
                    }
                    break;
                case R.id.next:
                    pos = (mAdapter.getSelectedPosition()+1) % mAdapter.getItemCount();
                    playSong(pos);
                    break;
            }
        }
    }

    private class StateChangeCallBack implements IStateChangeCallBack{

        @Override
        public void onCompletion() {
            int pos = (mAdapter.getSelectedPosition()+1) % mAdapter.getItemCount();
            playSong(pos);
        }

        @Override
        public void onPause() {
            pause();
        }

        @Override
        public void onResume() {
            resume();
        }
    }


}
