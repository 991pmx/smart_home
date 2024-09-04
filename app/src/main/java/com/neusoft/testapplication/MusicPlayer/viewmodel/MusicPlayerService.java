package com.neusoft.testapplication.MusicPlayer.viewmodel;

import android.app.Service;
import android.os.Binder;
import android.os.IBinder;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.media.MediaPlayer;


import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;

public class MusicPlayerService extends Service {
    private MediaPlayer mPlayer;
    public boolean isPlaying = false;
    public boolean isPaused = false;
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void playMusic(String path) throws IOException {
        if (mPlayer != null) {
            mPlayer.reset();
        } else {
            mPlayer = new MediaPlayer();
        }

        mPlayer.setDataSource(path);
        mPlayer.prepare();
        mPlayer.start();

        isPlaying = true;
        isPaused = false;

        if (completionListener != null) {
            mPlayer.setOnCompletionListener(completionListener);
        }
    }

    private MediaPlayer.OnCompletionListener completionListener;

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        this.completionListener = listener;
    }

    public void pauseMusic() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            isPlaying = false;
            isPaused = true;
        }
    }

    public void resumeMusic() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
            isPlaying = true;
            isPaused = false;
        }
    }

    public void seekTo(int position) {
        if (mPlayer != null) {
            mPlayer.seekTo(position);
        }
    }

    public int getCurrentPosition() {
        return mPlayer != null ? mPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mPlayer != null ? mPlayer.getDuration() : 0;
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        super.onDestroy();
    }
}

