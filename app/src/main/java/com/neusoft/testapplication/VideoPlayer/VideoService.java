package com.neusoft.testapplication.VideoPlayer;

import static com.neusoft.testapplication.utils.FileSave.writeVideoFile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.neusoft.testapplication.VideoPlayer.ROOM.Entity.Video;
import com.neusoft.testapplication.VideoPlayer.View.CustomVideoView;

import java.util.ArrayList;
import java.util.List;

public class VideoService extends Service {
    private static final String TAG = "Video";

    public static VideoPlayerManager videoManager;
    private List<Video> videoList=new ArrayList<>(); // 假设每个视频都是一个文件路径

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        if (videoManager != null) {
            videoManager.destroy();
            int i = videoManager.getCurrentIndex();
            Video video = videoList.get(i);
            writeVideoFile(this, video.getTitle(), videoManager.getPlayMode());

        }
        super.onDestroy();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (intent != null) {
            videoList = intent.getParcelableArrayListExtra("videoList");
            if (videoList != null && !videoList.isEmpty()) {
                if (videoManager == null) {
                    videoManager = new VideoPlayerManager();
                }
            }
        }
        return new MyBinder();
    }


    public class MyBinder extends Binder {
        public void init(CustomVideoView videoView, List<Video> videoList,Context context) {
            videoManager.init(videoView, videoList, context);
        }
        

        public void setPlayMode(int mode) {
            videoManager.setPlayMode(mode);
        }


        public int getcurrenttime() {
            return videoManager.getcurrenttime();
        }

        public boolean isIsrewinding() {
            return videoManager.isIsrewinding();
        }

        public void setIsrewinding(boolean b) {
            videoManager.setIsrewinding(b);
        }

        public Video playPrevious() {
            return videoManager.playPrevious();
        }

        public void rewind(Runnable runnable) {
            videoManager.rewind(runnable);
        }

        public float getCurrentSpeed() {
            return videoManager.getCurrentSpeed();
        }

        public void setCurrentSpeed(float v) {
            videoManager.setCurrentSpeed(v);
        }

        public Video playNext(int i) {
            return videoManager.playNext(i);
        }

        public void seekForward(float v) throws NoSuchFieldException, IllegalAccessException {
            videoManager.seekForward(v);
        }

        public void pauseVideo() {
            videoManager.pauseVideo();
        }

        public void playVideo(int currentIndex) {
            videoManager.playVideo(currentIndex);
        }
        public void playVideo() {
            videoManager.playVideo();
        }

        public void destroy() {
            videoManager.destroy();
        }

        public void setIsSmall(boolean b) {
            videoManager.setIsSmall(b);
        }

        public void setCurrentIndex(int i) {
            videoManager.setCurrentIndex(i);
        }

        public void setAutoUpdating(boolean b) {
            videoManager.setAutoUpdating(b);
        }


        public boolean isPlaying() {
            if (videoManager!=null) {
                return videoManager.isPlaying();
            }
            return false;
        }
        public int getCurrentIndex() {
            if (videoManager!=null) {
                return videoManager.getCurrentIndex();
            }
            return -1;
        }

        public Video getcurrentvideo() {
            return videoList.get(getCurrentIndex());
        }

        public int getPlayMode() {
            return videoManager.getPlayMode();
        }
    }


}