package com.example.mymusic.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.example.mymusic.Model.Song;

import java.io.IOException;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private AudioAttributes mPlaybackAttributes;
    private AudioFocusRequest mFocusRequest;
    private BroadcastReceiver mNoisyReceiver;
    private MusicPlayerBinder mBinder;
    private AssetFileDescriptor mAssetFileDescriptor;
    final Object mFocusLock = new Object();
    private boolean mUserPaused = false;
    private boolean mResumeOnFocusGain = false;
    private Handler handler;
    private IStateChangeCallBack mCallBack;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mBinder = new MusicPlayerBinder();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mPlaybackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        handler = new Handler();
        mFocusRequest = new AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setOnAudioFocusChangeListener(new MyFocusListener(),handler)
                .build();

        mNoisyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mBinder.pause();
            }
        };
        
        mMediaPlayer.setOnCompletionListener(mp -> {
            mCallBack.onCompletion();
        });
    }

    private class MusicPlayerBinder extends Binder implements IMusicBinder {

        @Override
        public void play() {
            if (mAssetFileDescriptor!=null && !mMediaPlayer.isPlaying()) {
                mUserPaused = false;
                mAudioManager.requestAudioFocus(mFocusRequest);
                mMediaPlayer.start();
                IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
                registerReceiver(mNoisyReceiver, filter);
            }
        }

        @Override
        public void pause() {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mUserPaused = true;
            }
        }

        @Override
        public void set(AssetFileDescriptor afd) {
        }

        @Override
        public void setAndPlay(AssetFileDescriptor afd) {
        }

        @Override
        public void setAndPlay(Song song) {
            set(song);
            play();
        }

        @Override
        public void set(Song song) {
            if (song == null) return;
            ContentResolver resolver = getApplicationContext().getContentResolver();
            String readOnlyMode = "r";
            try (AssetFileDescriptor assetFileDescriptor = resolver.openAssetFileDescriptor(song.getStreamUri(), readOnlyMode)) {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(assetFileDescriptor);
                mAssetFileDescriptor = assetFileDescriptor;
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isPlaying() {
            return mMediaPlayer.isPlaying();
        }

        @Override
        public void seekTo(int pos) {
            mMediaPlayer.seekTo(pos);
        }

        @Override
        public int getCurrentPosition() {
            return mMediaPlayer.getCurrentPosition();
        }

        @Override
        public void setCallBack(IStateChangeCallBack callBack) {
            mCallBack = callBack;
        }

        @Override
        public boolean hasSong() {
            return mAssetFileDescriptor!=null;
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private class MyFocusListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (!mUserPaused && mResumeOnFocusGain) {
                        synchronized (mFocusLock) {
                            mResumeOnFocusGain = false;
                        }
                        mMediaPlayer.start();
                        mCallBack.onResume();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    synchronized (mFocusLock) {
                        mResumeOnFocusGain = false;
                        mUserPaused = true;
                    }
                    mMediaPlayer.pause();
                    mCallBack.onPause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    synchronized (mFocusLock) {
                        mResumeOnFocusGain = true;
                    }
                    mMediaPlayer.pause();
                    mCallBack.onPause();
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
    }
}

