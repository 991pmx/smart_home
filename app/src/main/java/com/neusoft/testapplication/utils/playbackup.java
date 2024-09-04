package com.neusoft.testapplication.utils;

import android.content.Context;
import android.media.AudioManager;

import com.neusoft.testapplication.VideoPlayer.VideoPlayerManager;

public class playbackup {
    public static boolean pauseothers(Context context, VideoPlayerManager manager) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // 定义一个音频焦点变化的监听器
        AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    manager.pauseVideo();
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    manager.destroy();
                    audioManager.abandonAudioFocus(this);
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    manager.playVideo(manager.getCurrentIndex());
                }
            }
        };

        int result = audioManager.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            manager.playVideo(manager.getCurrentIndex());
        }
        return true;
    }

    public static String formatDuration(long millis) {
        StringBuilder sb = new StringBuilder();
        long durationInSeconds = millis / 1000;
        long hours = durationInSeconds / 3600;
        long minutes = (durationInSeconds % 3600) / 60;
        long seconds = durationInSeconds % 60;

        if (hours > 0) {
            sb.append(String.format("%02d:", hours));
        }
        sb.append(String.format("%02d:", minutes));
        sb.append(String.format("%02d", seconds));

        return sb.toString();
    }


}
