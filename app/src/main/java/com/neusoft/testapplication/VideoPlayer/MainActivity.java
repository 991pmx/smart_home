package com.neusoft.testapplication.VideoPlayer;

import static com.neusoft.testapplication.utils.FileSave.writeVideoFile;
import static com.neusoft.testapplication.utils.playbackup.formatDuration;
import static com.neusoft.testapplication.utils.playbackup.pauseothers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.neusoft.testapplication.R;
import com.neusoft.testapplication.VideoPlayer.ROOM.DAO.VideoDao;
import com.neusoft.testapplication.VideoPlayer.ROOM.Entity.Video;
import com.neusoft.testapplication.VideoPlayer.ROOM.VideoDatabase;
import com.neusoft.testapplication.VideoPlayer.View.CustomVideoView;
import com.neusoft.testapplication.VideoPlayer.ViewModel.ItemTouchcallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 101;
    private static final String TAG = "Video";
    private static MainActivity instance;
    private final Handler handler = new Handler();
    List<Video> videos;
    CustomVideoView videoView;
    ConstraintLayout actionplay;
    ConstraintLayout brightness_volume;
    ImageView pause;
    ImageView last;
    ImageView play;
    ImageView next;
    ImageView mode;
    ImageView tosmall;
    ImageView tofull;
    ImageView list;
    ImageView brightness_volumeImage;
    TextView start;
    TextView end;
    TextView speed;
    SeekBar seekBar;
    SeekBar brightness_volumeSeekBar;
    ImageButton home;
    RecyclerView recyclerView;
    FrameLayout container;
    int lastmode;
    VideoDao videoDAO;
    VideoAdapter adapter;
    private Runnable runnable;
    private Boolean isbtnVisible = true;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View floatingWindow;
    private ImageView pause_small;
    private ImageView play_small;
    private ImageView close;
    private TextView title;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.neusoft.CLOSE_WINDOW".equals(intent.getAction())) {
                Log.d(TAG, "onReceive: ");
                Destroywindow();
            }
        }
    };


    VideoService.MyBinder myBinder;
    ServiceConnection connection;
    private int currentmode;


    public static synchronized MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_detail);

        videos = getIntent().getParcelableArrayListExtra("videoList");
        lastmode = getIntent().getIntExtra("mode", 0);
        VideoDatabase db = VideoDatabase.getInstance(getApplicationContext());
        videoDAO = db.videoDao();
        Boolean isplaying = getIntent().getBooleanExtra("isplaying", false);
        if(isplaying){
            setContentView(R.layout.videoview);
            videoView = findViewById(R.id.videoView_full);
            BindService();
        }else{
            initbackhome();
            initrecyclerView();
            initgetall();
        }

    }
    @SuppressLint("ClickableViewAccessibility")
    private void initrecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new VideoAdapter(this, videos);
        recyclerView.setAdapter(adapter);

        ItemTouchcallback itemTouchcallback = new ItemTouchcallback(adapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(itemTouchcallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        adapter.setAdapterListener(new VideoAdapter.AdapterListener() {
            @Override
            public void play(int i) {
                initfull(i);
            }
            @Override
            public void dbdelete(int id) {
                videoDAO.deleteVideo(id);
            }

            @Override
            public void dbswap(int srcPosition, int dstPosition) {
                // TODO: 2024/9/2
            }
        });
    }


    private void initfull(int i) {
        Destroywindow();
        initlayout(i);
        pause.setOnClickListener(view -> {
            pausevideo();
        });

        play.setOnClickListener(view -> {
            playVideo();
        });

        last.setOnClickListener(view -> {
            playpre();

        });

        last.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startRewindDelayed();
                return false;
            }
        });

        next.setOnClickListener(view -> {
            playnext();
        });

        next.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                playForward();
                return false;
            }
        });
        mode.setOnClickListener(view -> {
            initpopup();
        });

        tosmall.setOnClickListener(view -> {
            getWindowPermission();
            toggleFullScreen();
            registerReceiver(receiver, new IntentFilter("com.neusoft.CLOSE_WINDOW"));
        });

        list.setOnClickListener(view -> {
            showlist();
        });
    }

    private void playpre() {
        if (myBinder.isIsrewinding()) {
            myBinder.setIsrewinding(false);
            speed.setVisibility(View.INVISIBLE);
            handler.removeCallbacks(runnable);
            return;
        }
        Video preVideo=myBinder.playPrevious();
        play.setVisibility(View.INVISIBLE);
        pause.setVisibility(View.VISIBLE);

        title.setText(preVideo.getTitle());
    }

    private void initpopup() {
        PopupMenu popup = new PopupMenu(MainActivity.this, mode);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.mode, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
//                                播放模式：0-列表循环，1-随机，2-单曲循环
            CharSequence title = item.getTitle();
            assert title != null;
            if (title.equals("repeat")) {// 处理第一个选项
                mode.setImageResource(R.drawable.repeat);
                currentmode=2;
                myBinder.setPlayMode(2);
                return true;
            } else if (title.equals("loop")) {// 处理第二个选项
                mode.setImageResource(R.drawable.loop);
                myBinder.setPlayMode(0);
                currentmode=0;
                return true;
            } else if (title.equals("random")) {// 处理第三个选项
                mode.setImageResource(R.drawable.random);
                myBinder.setPlayMode(1);
                currentmode=1;
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void playForward() {
        try {
            stratforward();
            speed.setText("Forwarding 2x >>");
            speed.setVisibility(View.VISIBLE);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Video playnext() {
        if (myBinder.getCurrentSpeed() == 2.0f) {
            speed.setVisibility(View.INVISIBLE);
            myBinder.setCurrentSpeed(1.0f);
            return null;
        }
        Video nextVideo=myBinder.playNext(1);
        pause.setVisibility(View.VISIBLE);
        play.setVisibility(View.INVISIBLE);
        title.setText(nextVideo.getTitle());
        return nextVideo;
    }

    private void BindService() {
        Intent serviceIntent = new Intent(this, VideoService.class);
        serviceIntent.putParcelableArrayListExtra("videoList", new ArrayList<>(videos));

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {


                myBinder=(VideoService.MyBinder)iBinder;
                initfull(myBinder.getCurrentIndex());

                myBinder.init(videoView, videos,MainActivity.this);
                myBinder.setIsSmall(false);
                int i=myBinder.getCurrentIndex();
                title.setText(videos.get(i).getTitle());
                myBinder.setCurrentIndex(i);
                VideoService.videoManager.setSeekbarListener(i1 -> runOnUiThread(() -> {
                    int currenttime = myBinder.getcurrenttime();
                    seekBar.setProgress(currenttime);
                    String currentDurationStr = formatDuration(currenttime);
                    start.setText(currentDurationStr);
                    seekBar.setMax((int) videos.get(i1).getDuration());
                    end.setText(formatDuration(videos.get(i1).getDuration()));
                }));
                myBinder.playVideo(i);



            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(serviceIntent,connection,BIND_AUTO_CREATE);

    }
    private void BindService(int i) {
        Intent serviceIntent = new Intent(this, VideoService.class);
        serviceIntent.putParcelableArrayListExtra("videoList", new ArrayList<>(videos));

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {


                myBinder=(VideoService.MyBinder)iBinder;
                myBinder.init(videoView, videos,MainActivity.this);
                myBinder.setIsSmall(false);
                title.setText(videos.get(i).getTitle());
                myBinder.setCurrentIndex(i);
                VideoService.videoManager.setSeekbarListener(i1 -> runOnUiThread(() -> {
                    int currenttime = myBinder.getcurrenttime();
                    seekBar.setProgress(currenttime);
                    String currentDurationStr = formatDuration(currenttime);
                    start.setText(currentDurationStr);
                    seekBar.setMax((int) videos.get(i1).getDuration());
                    end.setText(formatDuration(videos.get(i1).getDuration()));
                }));
                myBinder.playVideo(i);



            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(serviceIntent,connection,BIND_AUTO_CREATE);

    }
    private void initgetall() {
        ImageView getall = findViewById(R.id.getall);
        getall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //保存数据库
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //获取本机所有视频信息权限
                        getreadVideoPermission();

                        //删除之前数据
                        videoDAO.deleteVideos();


                        //获取本机所有视频信息
                        initVideoData();


                        //添加数据库
                        videoDAO.addVideos(videos);

                        
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //更新ui
                                adapter.notifyDataSetChanged(); // 通知适配器数据已更改
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void getreadVideoPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_VIDEO}, PackageManager.PERMISSION_GRANTED);
        }
    }

    private void initbackhome() {
        home = findViewById(R.id.HomeButton_details);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //隐藏状态栏和导航
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveVideoinDataBase();
        unregisterReceiver(receiver);
//        if (myBinder!=null) {
//            myBinder.destroy();
//        }

    }

    private void saveVideoinDataBase() {
        if(myBinder!=null) {
            Video video = myBinder.getcurrentvideo();
            writeVideoFile(this, video.getTitle(), myBinder.getPlayMode());
        }
    }


    /**
     * 当手机意外停止Activity时，保存当前播放音乐的状态
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveVideoinDataBase();

    }

    /**
     * 当手机内存不足强制关闭Activity时，重新执行Activity时恢复上次意外中断时的状态
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        saveVideoinDataBase();

    }




    private void startRewindDelayed() {
        speed.setText("<< Rewining");
        speed.setVisibility(View.VISIBLE);
        myBinder.setIsrewinding(true);
        runnable = new Runnable() {
            @Override
            public void run() {
                stratRewind(runnable);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void stratforward() throws NoSuchFieldException, IllegalAccessException {
        Log.d(TAG, "stratforward: ");
        myBinder.seekForward(2.0f);
    }

    private void stratRewind(Runnable runnable) {
        myBinder.rewind(runnable);
    }

    private void showlist() {
        setContentView(R.layout.activity_detail);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        VideoAdapter adapter = new VideoAdapter(MainActivity.this, videos);
        recyclerView.setAdapter(adapter);
        initbackhome();
    }


    private void playVideo() {
        if (pauseothers(this, VideoService.videoManager)) {
            myBinder.playVideo(myBinder.getCurrentIndex());
            play.setVisibility(View.INVISIBLE);
            pause.setVisibility(View.VISIBLE);
            if (play_small != null) {
                play_small.setVisibility(View.INVISIBLE);
                pause_small.setVisibility(View.VISIBLE);
            }

        }
    }

    private void pausevideo() {
        myBinder.pauseVideo();
        videos.get(myBinder.getCurrentIndex()).setBookmark(myBinder.getcurrenttime());
        play.setVisibility(View.VISIBLE);
        pause.setVisibility(View.INVISIBLE);
        if (play_small != null) {
            play_small.setVisibility(View.VISIBLE);
            pause_small.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("MissingInflatedId")
    private void initlayout(int i) {
        setContentView(R.layout.videoview);

        videoView = findViewById(R.id.videoView_full);

        BindService(i);

        actionplay = findViewById(R.id.actionplay_video);
        pause = findViewById(R.id.pause);
        last = findViewById(R.id.last);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        mode = findViewById(R.id.mode);
        tosmall = findViewById(R.id.tosmall);
        list = findViewById(R.id.list);
        start = findViewById(R.id.start);
        end = findViewById(R.id.end);
        speed = findViewById(R.id.speed);
        seekBar = findViewById(R.id.seekBar);
        title = findViewById(R.id.title);
        brightness_volumeImage = findViewById(R.id.brightness_volumeImage);
        brightness_volumeSeekBar = findViewById(R.id.brightness_volumeSeekBar);
        brightness_volume = findViewById(R.id.brightness_volume);

        switch (lastmode) {
            case 0:
                mode.setImageResource(R.drawable.loop);
                break;
            case 1:
                mode.setImageResource(R.drawable.random);
                break;
            case 2:
                mode.setImageResource(R.drawable.repeat);
                break;
            default:
                break;
        }





        seekBar.setMax((int) videos.get(i).getDuration());

        end.setText(formatDuration(videos.get(i).getDuration()));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    Video video=videos.get(myBinder.getCurrentIndex());
                    video.setBookmark(seekBar.getProgress());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            videoDAO.updateVideo(video);
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

        videoView.setMyGestureListener(new CustomVideoView.MyGestureListener() {
            @Override
            public void playorpause() {
                if (myBinder.isPlaying()) {
                    pausevideo();
                } else {
                    playVideo();
                }
            }

            @Override
            public void isbtnVisible() {
                if (isbtnVisible) {
                    actionplay.setVisibility(View.INVISIBLE);
                    isbtnVisible = false;
                } else {
                    actionplay.setVisibility(View.VISIBLE);
                    isbtnVisible = true;
                }
            }

            @Override
            public void adjustBrightness(float brightness) {
                brightness_volume.setVisibility(View.VISIBLE);
                try {
                    adjustBrightnessWithProgress(brightness);
                } catch (Settings.SettingNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void adjustVolume(float Volume) {
                brightness_volume.setVisibility(View.VISIBLE);
                GetChangeVolumePermission();
                adjustVolumeWithProgress(Volume);
            }
        });

    }

    // 音量调节增加进度条
    private void adjustVolumeWithProgress(float volume) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        brightness_volumeImage.setImageResource(R.drawable.voice);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "adjustVolumeWithProgress:currentVolume " + currentVolume);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "adjustVolumeWithProgress:volume  " + volume);

        int newVolumeIndex = (int) (currentVolume + volume * maxVolume);
        Log.d(TAG, "adjustVolumeWithProgress: newVolumeIndex " + newVolumeIndex);

        if (newVolumeIndex > maxVolume) {
            newVolumeIndex = maxVolume;
        } else if (newVolumeIndex < 0) {
            newVolumeIndex = 0;
        }

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolumeIndex, 0);
        brightness_volumeSeekBar.setMax(maxVolume);
        brightness_volumeSeekBar.setProgress(newVolumeIndex, true); // 更新进度条
        brightness_volume.postDelayed(new Runnable() {
            @Override
            public void run() {
                brightness_volume.setVisibility(View.INVISIBLE);
            }
        }, 2000);


    }


    // 亮度调节增加进度条
    private void adjustBrightnessWithProgress(float brightnessDelta) throws Settings.SettingNotFoundException {
        brightness_volumeImage.setImageResource(R.drawable.brightness);

        int currentBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);

        float newBrightness = currentBrightness + brightnessDelta * 255;

        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) newBrightness);

        brightness_volumeSeekBar.setMax(255);


        brightness_volumeSeekBar.setProgress((int) (newBrightness)); // 更新进度条

        brightness_volume.postDelayed(new Runnable() {
            @Override
            public void run() {
                brightness_volume.setVisibility(View.INVISIBLE);
            }
        }, 2000);


    }


    private void GetChangeVolumePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 200);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void toggleFullScreen() {
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout_full);

        if (floatingWindow == null) {
            constraintLayout.removeView(videoView);

            showlist();
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            floatingWindow = LayoutInflater.from(this).inflate(R.layout.floating_window, null);


            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float density = getdensity();

            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            layoutParams.gravity = Gravity.BOTTOM | Gravity.END; // 右下角

            // 根据屏幕密度调整位置
            int margin = (int) (10 * density);
            layoutParams.x = -margin;
            layoutParams.y = margin;

            // 设置新的尺寸
            layoutParams.width = (int) (videos.get(myBinder.getCurrentIndex()).getWidth() * 0.7f);
            layoutParams.height = (int) (videos.get(myBinder.getCurrentIndex()).getHeight() * 0.7f);


            container = floatingWindow.findViewById(R.id.container);
            tofull = container.findViewById(R.id.tofull);
            pause_small = container.findViewById(R.id.pause_small);
            play_small = container.findViewById(R.id.play_small);
            close = container.findViewById(R.id.close);
            FrameLayout.LayoutParams imageViewParams = (FrameLayout.LayoutParams) tofull.getLayoutParams();
            imageViewParams.gravity = Gravity.TOP | Gravity.START;
            tofull.setLayoutParams(imageViewParams);


            FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            centerParams.gravity = Gravity.CENTER;

            pause_small.setLayoutParams(centerParams);
            play_small.setLayoutParams(centerParams);

            FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            rightParams.gravity = Gravity.RIGHT|Gravity.TOP;

            close.setLayoutParams(rightParams);

            pause_small.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pausevideo();
                }
            });
            play_small.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playVideo();
                }
            });
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Destroywindow();
                    unbindService(connection);
                }
            });



            container.addView(videoView,centerParams);



            myBinder.setIsSmall(true);

            windowManager.addView(floatingWindow, layoutParams);

            FrameLayout.LayoutParams layoutParams_video = new FrameLayout.LayoutParams((int) (videos.get(myBinder.getCurrentIndex()).getWidth() * 0.7f),(int) (videos.get(myBinder.getCurrentIndex()).getHeight() * 0.7f));
            videoView.setLayoutParams(layoutParams_video);


            actionplay.setVisibility(View.INVISIBLE);
            isbtnVisible = false;

            tofull.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//                    Intent intent = new Intent("com.neusoft.ACTION_SHOW_DETAIL");
//                    sendBroadcast(intent);

                    unbindService(connection);
                    BindService();


                    Destroywindow();
                    setContentView(R.layout.videoview);
                    myBinder.setIsSmall(false);
                    ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout_full);
                    constraintLayout.addView(videoView);
                    myBinder.playVideo();
                }
            });

            videoView.setOnTouchListener(new View.OnTouchListener() {
                private float initialTouchX, initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            Log.d(TAG, "start: " + initialTouchX + "   " + initialTouchY);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            int deltaX = (int) (event.getRawX() - initialTouchX);
                            int deltaY = (int) (event.getRawY() - initialTouchY);

                            // 更新悬浮窗位置
                            layoutParams.x -= deltaX;
                            layoutParams.y -= deltaY;
                            windowManager.updateViewLayout(floatingWindow, layoutParams);

                            // 重置初始触摸坐标
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        default:
                            return false;
                    }
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    Video videonext=playnext();
                    layoutParams.width = (int) (videonext.getWidth() * 0.7f);
                    layoutParams.height = (int) (videonext.getHeight() * 0.7f);
                    windowManager.updateViewLayout(floatingWindow, layoutParams);

                    FrameLayout.LayoutParams layoutParams_video = new FrameLayout.LayoutParams(layoutParams.width,layoutParams.height);
                    videoView.setLayoutParams(layoutParams_video);


                }
            });
        } else {
            Destroywindow();
            constraintLayout.addView(videoView);
        }


    }

    private void Destroywindow() {
        if (windowManager != null) {
            windowManager.removeView(floatingWindow);
            windowManager = null;
            floatingWindow = null;
            layoutParams = null;
            container.removeView(videoView);
        }

    }

    private float getdensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    private void getWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @SuppressLint("Range")
    private void initVideoData() {

        videos = new ArrayList<>();

        String[] sLocalVideoColumns = {
                MediaStore.Video.Media._ID, // 视频id
                MediaStore.Video.Media.DATA, // 视频路径
                MediaStore.Video.Media.SIZE, // 视频字节大小
                MediaStore.Video.Media.TITLE, // 视频标题
                MediaStore.Video.Media.DURATION, // 视频时长
                MediaStore.Video.Media.ARTIST, // 艺人名称
                MediaStore.Video.Media.RESOLUTION, // 视频分辨率 X x Y格式
                MediaStore.Video.Media.DESCRIPTION, // 视频描述
                MediaStore.Video.Media.BOOKMARK // 上次视频播放的位置
        };

        String[] sLocalVideoThumbnailColumns = {
                MediaStore.Video.Thumbnails.DATA, // 视频缩略图路径
                MediaStore.Video.Thumbnails.VIDEO_ID, // 视频id
                MediaStore.Video.Thumbnails.KIND,
                MediaStore.Video.Thumbnails.WIDTH, // 视频缩略图宽度
                MediaStore.Video.Thumbnails.HEIGHT // 视频缩略图高度
        };

        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, sLocalVideoColumns,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Video video = new Video();
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST));
                String resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                String description = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION));
                int bookmark = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.BOOKMARK));

                Cursor thumbnailCursor = getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, sLocalVideoThumbnailColumns,
                        MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null);
                if (thumbnailCursor != null && thumbnailCursor.moveToFirst()) {
                    do {
                        String thumbnailData = thumbnailCursor.getString(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                        int kind = thumbnailCursor.getInt(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.KIND));
                        long width = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.WIDTH));
                        long height = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.HEIGHT));

                        video.setThumbnailData(thumbnailData);
                        video.setKind(kind);
                        video.setWidth(width);
                        video.setHeight(height);
                    } while (thumbnailCursor.moveToNext());

                    thumbnailCursor.close();
                }

                video.setId(id);
                video.setData(data);
                video.setSize(size);
                video.setTitle(title);
                video.setDuration(duration);
                video.setArtist(artist);
                video.setDescription(description);
                video.setBookmark(bookmark);

                String[] resolutionParts = resolution.split("×");

                int width = Integer.parseInt(resolutionParts[0]);
                int height = Integer.parseInt(resolutionParts[1]);

                video.setWidth(width);
                video.setHeight(height);

                videos.add(video);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }


}