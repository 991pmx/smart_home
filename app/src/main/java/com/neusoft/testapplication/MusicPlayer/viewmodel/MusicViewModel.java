package com.neusoft.testapplication.MusicPlayer.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.neusoft.testapplication.MusicPlayer.model.MusicDatabaseHelper;
import com.neusoft.testapplication.MusicPlayer.viewmodel.utils.MusicMetadataRetriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicViewModel extends AndroidViewModel {

    public enum PlayMode {
        LOOP_ALL,    // 列表循环
        LOOP_ONE     // 单曲循环
    }
    private final MutableLiveData<PlayMode> playMode = new MutableLiveData<>(PlayMode.LOOP_ALL);

    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>();
    private final MutableLiveData<Integer> duration = new MutableLiveData<>();
    private final MutableLiveData<MusicMetadataRetriever.MusicMetadata> musicMetadata = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();
    private final MutableLiveData<Map<Long, String>> lyricsMap = new MutableLiveData<>(); // 时间戳和歌词的映射

    private final Handler speedHandler = new Handler(Looper.getMainLooper());
    private Runnable speedRunnable;


    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updatePositionRunnable;
    private String currentPath;
    private int currentIndex = -1;  // 当前播放音乐的索引
    private List<MusicDatabaseHelper.Music> musicList;

    private MusicPlayerService musicPlayerService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicPlayerService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayerService = null;
        }
    };

    public MusicViewModel(@NonNull Application application) {
        super(application);
        Intent intent = new Intent(application, MusicPlayerService.class);
        application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public LiveData<Integer> getCurrentPosition() {
        return currentPosition;
    }

    public LiveData<Integer> getDuration() {
        return duration;
    }

    public LiveData<MusicMetadataRetriever.MusicMetadata> getMusicMetadata() {
        return musicMetadata;
    }

    public LiveData<Boolean> isPlaying() {
        return isPlaying;
    }

    public LiveData<PlayMode> getPlayMode() {
        return playMode;
    }

    public LiveData<Map<Long, String>> getLyricsMap() {
        return lyricsMap;
    }

    public void togglePlayPause() {
        if (musicPlayerService.isPlaying) {
            pauseMusic();
            stopUpdatingPosition(); // 停止进度条更新
            isPlaying.postValue(false);  // 通知UI更新
        } else if (musicPlayerService.isPaused) {
            resumeMusic();
            startUpdatingPosition(); // 恢复进度条更新
            isPlaying.postValue(true);  // 通知UI更新
        } else if (currentPath != null) {
            playMusic(currentPath);
        }
    }

    public void togglePlayMode() {
        if (playMode.getValue() == PlayMode.LOOP_ALL) {
            playMode.setValue(PlayMode.LOOP_ONE);
        } else {
            playMode.setValue(PlayMode.LOOP_ALL);
        }
    }

    public void playMusic(String path) {
        try {
            loadMusicMetadata(path);
            loadLyrics(path);
            musicPlayerService.playMusic(path);
            duration.postValue(musicPlayerService.getDuration());
            startUpdatingPosition();
            isPlaying.postValue(true); // 通知UI更新播放按钮状态

            // 设置播放完成的监听器，根据播放模式执行不同操作
            musicPlayerService.setOnCompletionListener(mp -> {
                if (playMode.getValue() == PlayMode.LOOP_ONE) {
                    // 单曲循环，重新播放当前歌曲
                    playMusic(currentPath);
                } else if (playMode.getValue() == PlayMode.LOOP_ALL) {
                    // 列表循环，播放下一首
                    playNext();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPath = path;
    }

    public void pauseMusic() {
        musicPlayerService.pauseMusic();
        isPlaying.postValue(false);
    }

    public void resumeMusic() {
        musicPlayerService.resumeMusic();
        isPlaying.postValue(true);
    }

    public void setMusicList(List<MusicDatabaseHelper.Music> musicList) {
        this.musicList = musicList;
    }

    public void playMusicAtIndex(int index) {
        if (musicList != null && index >= 0 && index < musicList.size()) {
            currentIndex = index;
            playMusic(musicList.get(index).getPath());
        }
    }

    public void playNext() {
        if (musicList != null) {
            if (currentIndex < musicList.size() - 1) {
                playMusicAtIndex(currentIndex + 1);
            } else {
                // 如果当前是最后一首，则播放第一首
                playMusicAtIndex(0);
            }
        }
    }

    public void playPrevious() {
        if (musicList != null) {
            if (currentIndex > 0) {
                playMusicAtIndex(currentIndex - 1);
            } else {
                // 如果当前是第一首，则播放最后一首
                playMusicAtIndex(musicList.size() - 1);
            }
        }
    }

    public void startFastForward() {
        stopFastForwardOrRewind(); // 先停止可能已经在运行的快进或快退
        speedRunnable = new Runnable() {
            @Override
            public void run() {
                if (musicPlayerService != null && musicPlayerService.isPlaying) {
                    int currentPos = musicPlayerService.getCurrentPosition(); // 获取当前播放位置
                    int newPosition = currentPos + 2000; // 每次快进2秒
                    musicPlayerService.seekTo(newPosition);
                    currentPosition.postValue(newPosition); // 更新LiveData
                    speedHandler.postDelayed(this, 200); // 每200ms快进一次
                }
            }
        };
        speedHandler.post(speedRunnable);
    }

    public void startRewind() {
        stopFastForwardOrRewind(); // 先停止可能已经在运行的快进或快退
        speedRunnable = new Runnable() {
            @Override
            public void run() {
                if (musicPlayerService != null && musicPlayerService.isPlaying) {
                    int currentPos = musicPlayerService.getCurrentPosition(); // 获取当前播放位置
                    int newPosition = currentPos - 2000; // 每次快退2秒
                    musicPlayerService.seekTo(newPosition);
                    currentPosition.postValue(newPosition); // 更新LiveData
                    speedHandler.postDelayed(this, 200); // 每200ms快退一次
                }
            }
        };
        speedHandler.post(speedRunnable);
    }

    public void stopFastForwardOrRewind() {
        if (speedRunnable != null) {
            speedHandler.removeCallbacks(speedRunnable);
            speedRunnable = null;
        }
    }

    public void seekTo(int position) {
        musicPlayerService.seekTo(position);
        currentPosition.postValue(position);
    }

    public void startUpdatingPosition() {
        updatePositionRunnable = new Runnable() {
            @Override
            public void run() {
                if (musicPlayerService != null && musicPlayerService.isPlaying) {
                    int position = musicPlayerService.getCurrentPosition();
                    currentPosition.postValue(position);
                    handler.postDelayed(this, 1000); // 每秒更新一次
                }
            }
        };
        handler.post(updatePositionRunnable); // 开始更新进度条
    }

    public void stopUpdatingPosition() {
        if (handler != null && updatePositionRunnable != null) {
            handler.removeCallbacks(updatePositionRunnable); // 停止进度条更新
        }
    }

    public void loadLyrics(String path) {
        Map<Long, String> lyrics = new LinkedHashMap<>(); // 使用LinkedHashMap来保持顺序
        File lrcFile = new File(path.replace(".mp3", ".lrc"));
        if (!lrcFile.exists()) {
            lyricsMap.postValue(null); // 如果歌词文件不存在，返回null
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(lrcFile))) {
            String line;
            Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{1,2})\\]"); // 支持1到2位小数
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    long minutes = Long.parseLong(matcher.group(1));
                    long seconds = Long.parseLong(matcher.group(2));
                    long milliseconds = Long.parseLong(matcher.group(3));

                    if (milliseconds < 10) {
                        milliseconds *= 100;  // 处理1位小数
                    } else if (milliseconds < 100) {
                        milliseconds *= 10;  // 处理2位小数
                    }

                    long time = minutes * 60 * 1000 + seconds * 1000 + milliseconds;
                    String text = line.substring(matcher.end()).trim();

                    if (!text.isEmpty()) {
                        lyrics.put(time, text);
                    }
                }
            }
            if (lyrics.isEmpty()) {
                lyricsMap.postValue(null); // 如果歌词文件为空，返回null
            } else {
                lyricsMap.postValue(lyrics);
            }
        } catch (IOException e) {
            e.printStackTrace();
            lyricsMap.postValue(null); // 在发生IO异常时，也返回null
        }
    }





    public void loadMusicMetadata(String path) {
        new Thread(() -> {
            try {
                MusicMetadataRetriever.MusicMetadata metadata = MusicMetadataRetriever.retrieveMetadata(path);
                musicMetadata.postValue(metadata);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        getApplication().unbindService(serviceConnection);
    }
}

