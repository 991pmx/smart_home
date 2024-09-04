package com.neusoft.testapplication.VideoPlayer;


import android.content.Context;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.neusoft.testapplication.VideoPlayer.ROOM.Entity.Video;
import com.neusoft.testapplication.VideoPlayer.View.CustomVideoView;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;


public class VideoPlayerManager {
    private static final String TAG = "Video";
    private final Handler handler = new Handler();
    SeekbarListener seekbarListener;
    List<Video> videoList; // 视频URI列表
    CustomVideoView videoView;
    Context context;
    MediaPlayer mediaPlayer;
    private boolean isrewinding = false;
    private boolean isAutoUpdating = true;
    private Runnable updateProgressRunnable;
    private int currentIndex = 0; // 当前播放的视频索引
    private int playMode = 0; // 播放模式：0-列表循环，1-随机，2-单曲循环
    private float currentSpeed = 1f;
    private boolean isSmall;


    public void setAutoUpdating(boolean autoUpdating) {
        isAutoUpdating = autoUpdating;
    }

    public void setSeekbarListener(SeekbarListener seekbarListener) {
        this.seekbarListener = seekbarListener;
    }


    public boolean isIsrewinding() {
        return isrewinding;
    }

    public void setIsrewinding(boolean isrewinding) {
        this.isrewinding = isrewinding;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(float currentSpeed) {
        this.currentSpeed = currentSpeed;
        PlaybackParams playbackParams = new PlaybackParams();
        playbackParams.setSpeed(currentSpeed);
        mediaPlayer.setPlaybackParams(playbackParams);
    }

    public void init(CustomVideoView videoView, List<Video> videoList,Context context) {
        this.videoView = videoView;
        this.videoList = videoList;
        this.context=context;
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playNext(0);
            }
        });

        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoUpdating && seekbarListener != null) {
                    seekbarListener.updateseekbar(currentIndex);
                    handler.postDelayed(this, 1);
                }
            }
        };
    }

    public void startAutoUpdate() {
        isAutoUpdating = true;
        handler.postDelayed(updateProgressRunnable, 0);
    }

    private void initvideoView(int currentVideoIndex) {
        Video video = videoList.get(currentVideoIndex);
        long videoWidth = video.getWidth();
        long videoHeight = video.getHeight();
        float aspectRatio = (float) videoWidth / (float) videoHeight;


        ConstraintLayout parentLayout = (ConstraintLayout) videoView.getParent();
            int width = parentLayout.getWidth();
            int height = parentLayout.getHeight();
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int newWidth, newHeight;
        if (width>screenWidth)
            width=screenWidth;
        if (height>screenHeight)
            height=screenHeight;
        if (videoWidth > videoHeight) {
            newWidth = width;
            newHeight =height;
        } else {
            newHeight = height;
            newWidth = (int) (height * aspectRatio);
        }

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(newWidth, newHeight);
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        videoView.setLayoutParams(layoutParams);
    }


    // 播放视频
    public void playVideo(int i) {
        if (videoView != null) {
            if (!isSmall) {
                initvideoView(i);
            }
            videoView.setVideoURI(Uri.parse(videoList.get(i).getData()));
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    videoView.seekTo(videoList.get(i).getBookmark());
                    videoView.start();
//                    Log.d(TAG, "playVideo: " + videoView.getCurrentPosition());
                    startAutoUpdate();
                }
            });
        }
    }

    //    继续播放当前视频
    public void playVideo() {
        int i = currentIndex;
        if (videoView != null) {
            videoView.setVideoURI(Uri.parse(videoList.get(i).getData()));
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    videoView.seekTo(videoList.get(i).getBookmark());
                    videoView.start();
//                    Log.d(TAG, "playVideo: " + videoView.getCurrentPosition());
                    startAutoUpdate();
                }
            });
        }
    }

    // 从头播放视频
    public void playVideo(int i, int bookmark) {
        if (videoView != null) {
            videoView.setVideoURI(Uri.parse(videoList.get(i).getData()));
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    videoView.start();
                }
            });
        }
    }

    // 暂停视频
    public void pauseVideo() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
            videoList.get(currentIndex).setBookmark(videoView.getCurrentPosition());
//            Log.d(TAG, "pauseVideo: " + videoView.getCurrentPosition());
        }
    }

    // 播放上一曲
    public Video playPrevious() {
        if (videoList.size() == 0) return null;
        currentIndex = (currentIndex - 1 + videoList.size()) % videoList.size();
        playVideo(currentIndex);
        seekbarListener.updateseekbar(currentIndex);
        return videoList.get(currentIndex);
    }

    // 播放下一曲
    public Video playNext(int mode) {
        //mode 0-自动续播  1-点击下一曲
        if (videoList.size() == 0) return null;
        if (playMode == 1) {
            // 随机播放模式下，random索引
            currentIndex = new Random().nextInt(videoList.size());
            videoList.get(currentIndex).setBookmark(0);
            playVideo(currentIndex);
            return null;
        }
        if (playMode == 2 && mode == 0) {
            // 从头播放
            playVideo(currentIndex, 0);
            return null;
        }
        currentIndex = (currentIndex + 1) % videoList.size();
        playVideo(currentIndex);
        return videoList.get(currentIndex);
    }

    //快进
    public void seekForward(float speed) throws NoSuchFieldException, IllegalAccessException {
        if (videoView != null && videoView.isPlaying()) {
            Field mediaPlayerField = VideoView.class.getDeclaredField("mMediaPlayer");
            mediaPlayerField.setAccessible(true);
            mediaPlayer = (MediaPlayer) mediaPlayerField.get(videoView);
            if (mediaPlayer != null) {
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed(speed);
                currentSpeed = speed;
                mediaPlayer.setPlaybackParams(playbackParams);
            }
        }
    }

    // 快退
    public void rewind(Runnable runnable) {
        long currentTimeMillis = videoView.getCurrentPosition(); // 获取当前播放位置
        long seekBackMillis = 500; // 假设每次回退1秒
        if (currentTimeMillis - seekBackMillis >= 0 && isIsrewinding()) {
            videoView.seekTo((int) (currentTimeMillis - seekBackMillis)); // 回退播放位置
            handler.postDelayed(runnable, 1); // 每隔1秒再次执行
        } else {
            // 如果已经到达视频开头，则停止定时器
            handler.removeCallbacks(runnable);
        }
    }

    public int getcurrenttime() {
        return videoView.getCurrentPosition();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    //    当前播放歌曲
    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getPlayMode() {
        return playMode;
    }

    // 设置播放模式
    public void setPlayMode(int mode) {
        this.playMode = mode;
    }

    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void pauseVideo(CustomVideoView videoView) {
        this.videoView = videoView;
        playVideo(currentIndex);
    }

    public Boolean isPlaying() {
        return videoView.isPlaying();
    }

    public void prepareVideo(int i) {
        playVideo(i);
        pauseVideo();
    }

    public void setIsSmall(boolean b) {
        this.isSmall = b;
        videoView.setIssmall(b);
    }

    public interface SeekbarListener {
        void updateseekbar(int currentindex);
    }


}

