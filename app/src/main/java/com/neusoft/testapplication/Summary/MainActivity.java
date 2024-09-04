package com.neusoft.testapplication.Summary;

import static com.neusoft.testapplication.utils.FileSave.readVideoFile;
import static com.neusoft.testapplication.utils.playbackup.formatDuration;
import static com.neusoft.testapplication.utils.playbackup.pauseothers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.neusoft.testapplication.Device.DeviceActivity;
import com.neusoft.testapplication.Device.ModeActivity;
import com.neusoft.testapplication.R;
import com.neusoft.testapplication.VideoPlayer.ROOM.DAO.VideoDao;
import com.neusoft.testapplication.VideoPlayer.ROOM.Entity.Video;
import com.neusoft.testapplication.VideoPlayer.ROOM.VideoDatabase;
import com.neusoft.testapplication.VideoPlayer.VideoPlayerManager;
import com.neusoft.testapplication.VideoPlayer.VideoService;
import com.neusoft.testapplication.VideoPlayer.View.CustomVideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressLint("MissingInflatedId")

public class MainActivity extends AppCompatActivity implements VolumeChangeObserver.VolumeChangeListener {
    private static final String TAG = "Video";
    VideoDao videoDao;
    TextView voice;
    List<Video> videos;
    Handler handler = new Handler();
    Runnable runnable;
    ImageView pause;
    ImageView last;
    ImageView play;
    ImageView next;
    ImageView tomain;
    SeekBar seekBar;
    TextView start;
    TextView end;
    CustomVideoView videoView;
    int lastVideoIndex;
    int currentVideoIndex;
    int lastMode;
    VideoDatabase videoDatabase;
    VideoService.MyBinder myBinder;
    ServiceConnection connection;
    ConstraintLayout videoModule;
    ImageView music_tomain;


    private VolumeChangeObserver mVolumeChangeObserver;

    @Override
    protected void onResume() {
        mVolumeChangeObserver.registerReceiver();
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());

                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }

        //数据库获取上次的list
        initData();

    }

    @Override
    protected void onDestroy() {
        mVolumeChangeObserver.unregisterReceiver();
        unbindService(connection);
        super.onDestroy();
        saveVideoData();
    }

    @Override
    public void onVolumeChanged(int volume) {
        //系统媒体音量改变时的回调
        voice.setText("" + volume*100/15);
    }

    private void saveVideoData() {

    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView time = findViewById(R.id.time);
        voice = findViewById(R.id.voicetext);
        ImageView shadow_device = findViewById(R.id.shadow_device);
        ImageView shadow_video = findViewById(R.id.shadow_video);
        ImageView shadow_music = findViewById(R.id.shadow_music);
        pause = findViewById(R.id.pause);
        last = findViewById(R.id.last);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        tomain = findViewById(R.id.tomain);
        seekBar = findViewById(R.id.seekBar_main);
        start = findViewById(R.id.start_main);
        end = findViewById(R.id.end_main);
        ImageView home = findViewById(R.id.Home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTaskToBack(true);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    Video video=videos.get(myBinder.getCurrentIndex());
                    video.setBookmark(seekBar.getProgress());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            videoDao.updateVideo(video);
                        }
                    }).start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                myBinder.setAutoUpdating(false);
                pausevideo();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playVideo();
            }
        });


        videoView = findViewById(R.id.videoView);
        shadow_video.setVisibility(View.INVISIBLE);

        music_tomain = findViewById(R.id.music_tomain);


        // 获取当前时间
        Date date = new Date();
        // 创建一个SimpleDateFormat对象并设置格式
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm a", Locale.getDefault());
        // 使用SimpleDateFormat对象格式化Date对象
        String formattedDate = sdf.format(date);
        // 显示时间
        time.setText(formattedDate);


        // 读取设备数据
        File files = new File(getFilesDir(), "device.txt");
        StringBuilder content = new StringBuilder(); // 用于存储文件内容
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("device.txt")))) {
            String line;
            line = reader.readLine();
            String state = line;
            //分析并显示
            int number = Integer.parseInt(state);
            int num = number / 1000 + number / 100 % 10 + number % 100 / 10 + number % 10;
            DeviceActivity.setac(number / 1000);
            DeviceActivity.setsoc(number / 100 % 10);
            DeviceActivity.setap(number % 100 / 10);
            DeviceActivity.sethum(number % 10);
            String Num = Integer.toString(num) + " Device";
            TextView devicenumber = findViewById(R.id.device_number);
            devicenumber.setText(Num);
            int mode = ModeActivity.initmode();
            TextView Text = findViewById(R.id.which_mode);
            switch (mode) {
                case 1:
                    Text.setText("Coming Home Mode");
                    break;
                case 2:
                    Text.setText("Leaving Home Mode");
                    break;
                case 3:
                    Text.setText("Energy Saving Mode");
                    break;
                case 4:
                    Text.setText("Sleeping Mode");
                    break;
                default:
                    Text.setText("Not In Any Mode");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 处理异常
        }

        //监听设备模块
        ConstraintLayout constraintLayout_device = findViewById(R.id.deviceModule);
        constraintLayout_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在此处处理点击事件
                Intent it = new Intent(MainActivity.this, DeviceActivity.class);
                startActivity(it);
            }
        });


        videoModule = findViewById(R.id.videoModule);
        videoModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shadow_video.setVisibility(View.VISIBLE);
            }
        });

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取媒体音量
        voice.setText(Integer.toString(currentVolume*100/15));

        mVolumeChangeObserver = new VolumeChangeObserver(this);
        mVolumeChangeObserver.setVolumeChangeListener(this);
        videos = new ArrayList<>();


        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausevideo();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });

        last.setOnClickListener(view -> {
            if (myBinder.isIsrewinding()) {
                myBinder.setIsrewinding(false);
                handler.removeCallbacks(runnable);
                return;
            }
            myBinder.playPrevious();
            play.setVisibility(View.INVISIBLE);
            pause.setVisibility(View.VISIBLE);
        });

        last.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                myBinder.setIsrewinding(true);
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        myBinder.rewind(runnable);
                    }
                };
                handler.postDelayed(runnable, 1000);
                return false;
            }
        });

        next.setOnClickListener(view -> {
            if (myBinder.getCurrentSpeed() == 2.0f) {
                myBinder.setCurrentSpeed(1.0f);
                return;
            }
            myBinder.playNext(1);
            pause.setVisibility(View.VISIBLE);
            play.setVisibility(View.INVISIBLE);
        });

        next.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    myBinder.seekForward(2.0f);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        });


        tomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intoVideoActivity();
            }
        });

        music_tomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, com.neusoft.testapplication.MusicPlayer.View.MusicActivity.class);
                intent.putParcelableArrayListExtra("videoList", new ArrayList<>(videos));
                intent.putExtra("mode", lastMode);
                intent.putExtra("isplaying", myBinder.isPlaying());

                startActivity(intent);
            }
        });
    }

    public void intoVideoActivity() {
        play.setVisibility(View.VISIBLE);
        pause.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(MainActivity.this, com.neusoft.testapplication.VideoPlayer.MainActivity.class);
        intent.putParcelableArrayListExtra("videoList", new ArrayList<>(videos));
        intent.putExtra("mode", lastMode);
        intent.putExtra("isplaying", myBinder.isPlaying());

        startActivity(intent);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: ");
    }


    private void playVideo() {

        sendBroadcast(new Intent("com.neusoft.CLOSE_WINDOW"));

        if (videos.size() > 0) {//点击播放，背景变成黑色
            Context context = getApplicationContext();
            int color = ContextCompat.getColor(context, R.color.black);
            videoModule.setBackground(new ColorDrawable(color));
            videoView.setVisibility(View.VISIBLE);
            if (pauseothers(MainActivity.this, VideoService.videoManager)) {//closeother
                myBinder.playVideo(currentVideoIndex);
                play.setVisibility(View.INVISIBLE);
                pause.setVisibility(View.VISIBLE);
            }
        }
    }

    private void pausevideo() {
        myBinder.pauseVideo();
        videos.get(myBinder.getCurrentIndex()).setBookmark(myBinder.getcurrenttime());
        play.setVisibility(View.VISIBLE);
        pause.setVisibility(View.INVISIBLE);
    }


    private void initData() {
        videoDatabase = VideoDatabase.getInstance(this);
        videoDao = videoDatabase.videoDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                videos = videoDao.getAllVideos();
                bindService();

            }
        }).start();
    }

    private void initTimeText(int currentindex) {
        if (currentindex > -1) {
            String currentDurationStr = formatDuration(videos.get(currentindex).getBookmark());
            start.setText(currentDurationStr);
            seekBar.setMax((int) videos.get(currentindex).getDuration());
            end.setText(formatDuration(videos.get(currentindex).getDuration()));
        }
    }

    private void initthumbnail() {
        //如果本机存储有视频，
        if (videos.size() > 0) {//设置上次播放的视频的缩略图
            String message = readVideoFile(MainActivity.this);
            Video video;
            if (message != null) {//如果上次保存了数据
                String[] parts = message.split(",");

                String Title = parts[0];
                String mode = parts[1];

                lastVideoIndex = findVideoIndexByTitle(Title);
                lastMode = Integer.parseInt(mode);
                myBinder.setPlayMode(Integer.parseInt(mode));
                currentVideoIndex = lastVideoIndex;
                myBinder.setCurrentIndex(currentVideoIndex);
                video = videos.get(lastVideoIndex);
            } else {//如果没有数据
                video = videos.get(0);
                currentVideoIndex = 0;
            }

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(video.getData()); // 设置视频文件的路径
                // 获取视频的某一帧作为缩略图，这里以第一帧为例
                Bitmap thumbnail = retriever.getFrameAtTime(video.getBookmark(), MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoModule.setBackground(new BitmapDrawable(getResources(), thumbnail));
                    }
                });

            } catch (IllegalArgumentException ex) {
            } finally {
                try {
                    retriever.release(); // 释放资源
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            currentVideoIndex = -1;
        }

    }


    public int findVideoIndexByTitle(String Title) {
        for (int i = 0; i < videos.size(); i++) {
            if (videos.get(i).getTitle().equals(Title)) {
                return i;
            }
        }
        return -1;
    }


    private void bindService() {
        Intent serviceIntent = new Intent(MainActivity.this, VideoService.class);
        serviceIntent.putParcelableArrayListExtra("videoList", new ArrayList<>(videos));

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                myBinder = (VideoService.MyBinder) iBinder;

                if (VideoService.videoManager != null) {
                    VideoService.videoManager.setSeekbarListener(new VideoPlayerManager.SeekbarListener() {
                        @Override
                        public void updateseekbar(int currentindex) {
                            int currenttime = VideoService.videoManager.getcurrenttime();
                            seekBar.setProgress(currenttime);
                            String currentDurationStr = formatDuration(currenttime);
                            start.setText(currentDurationStr);
                            seekBar.setMax((int) videos.get(currentindex).getDuration());
                            end.setText(formatDuration(videos.get(currentindex).getDuration()));
                        }
                    });
                    myBinder.init(videoView,videos,MainActivity.this);
                    VideoService.videoManager.prepareVideo(0);
                    VideoService.videoManager.setIsSmall(true);

                }

                initthumbnail();
                initTimeText(currentVideoIndex);

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

    }




}