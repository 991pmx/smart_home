package com.neusoft.testapplication.MusicPlayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.neusoft.testapplication.R;

import java.io.IOException;

public class MusicPlayerController {

    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private boolean isPaused = false; // 添加这个标记来跟踪是否处于暂停状态
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SeekBar musicProgress;
    private final ImageButton btn_play_pause;
    private final TextView totalDurationTextView;
    private final TextView playingDurationTextView;
    private String currentPath;  // 用于保存当前播放的音乐路径

    public MusicPlayerController(Context context, SeekBar musicProgress, ImageButton btn_play_pause,
                                 TextView totalDurationTextView, TextView playingDurationTextView) {
        this.musicProgress = musicProgress;
        this.btn_play_pause = btn_play_pause;
        this.totalDurationTextView = totalDurationTextView;
        this.playingDurationTextView = playingDurationTextView;

        setupSeekBar();
    }

    private void setupSeekBar() {
        musicProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mPlayer != null) {
                    mPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlayer != null && isPlaying) {
                    mPlayer.seekTo(seekBar.getProgress());
                    updateSeekBar();
                }
            }
        });

        btn_play_pause.setOnClickListener(v -> togglePlayPause());
    }

    public void togglePlayPause() {
        if (isPlaying) {
            pauseMusic();
        } else if (isPaused) {
            resumeMusic(); // 恢复播放
        } else if (currentPath != null) {
            playMusic(currentPath);
        }
    }

    public void playMusic(String path) {
        if (path == null || path.isEmpty()) {
            return; // 如果路径无效，直接返回
        }

        if (mPlayer != null) {
            mPlayer.reset();
        } else {
            mPlayer = new MediaPlayer();
        }

        currentPath = path;  // 保存当前播放的音乐路径
        isPaused = false; // 重置暂停标记

        try {
            mPlayer.setDataSource(path);

            // 使用Handler进行异步prepare
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                btn_play_pause.setImageResource(R.drawable.pause);
                musicProgress.setMax(mp.getDuration());
                totalDurationTextView.setText(formatDuration(mp.getDuration()));
                updateSeekBar();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resumeMusic() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
            isPlaying = true;
            isPaused = false; // 重置暂停标记
            btn_play_pause.setImageResource(R.drawable.pause);
            updateSeekBar();
        }
    }

    private void pauseMusic() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            isPlaying = false;
            isPaused = true; // 标记为暂停状态
            btn_play_pause.setImageResource(R.drawable.play);
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    private void updateSeekBar() {
        if (mPlayer != null) {
            int currentPosition = mPlayer.getCurrentPosition();
            musicProgress.setProgress(currentPosition);
            playingDurationTextView.setText(formatDuration(currentPosition));
            handler.postDelayed(updateSeekBarRunnable, 1000);
        }
    }

    private final Runnable updateSeekBarRunnable = this::updateSeekBar;

    private String formatDuration(int duration) {
        int minutes = (duration / 1000) / 60;
        int seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        handler.removeCallbacks(updateSeekBarRunnable);
    }
}



