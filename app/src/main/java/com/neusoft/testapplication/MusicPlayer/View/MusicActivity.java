package com.neusoft.testapplication.MusicPlayer.View;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import com.neusoft.testapplication.MusicPlayer.MusicPlayerController;
import com.neusoft.testapplication.MusicPlayer.View.adapter.MusicListAdapter;
import com.neusoft.testapplication.MusicPlayer.model.MusicDatabaseHelper;
import com.neusoft.testapplication.MusicPlayer.viewmodel.MusicViewModel;
import com.neusoft.testapplication.MusicPlayer.viewmodel.utils.MusicFormatConverter;
import com.neusoft.testapplication.R;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MusicActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView; // 侧栏布局

    private RecyclerView recyclerView;
    private ImageView musicCover;
    private TextView songTitle;
    private TextView artistName;
    private ScrollView lyricsScrollView; // 元数据
    private LinearLayout lyricsContainer;

    private TextView textView_totalDuration;
    private TextView textView_playingDuration;
    private ImageButton btn_music_list;
    private ImageButton btn_play_pause;
    private ImageButton btn_rewind;
    private ImageButton btn_previous;
    private ImageButton btn_next;
    private ImageButton btn_fast_forward;
    private ImageButton btn_background_play;
    private ImageButton btn_play_mode;
    private SeekBar SeekBar;  // 播放控件

    private final Handler handler = new Handler(Looper.getMainLooper());

    private MusicViewModel musicViewModel;
    private List<MusicDatabaseHelper.Music> musics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        // 请求权限，初始化UI
        checkAndRequestPermissions();
        // 异步加载音乐列表并初始化UI
        handler.post(() -> {
            MusicDatabaseHelper dbHelper = new MusicDatabaseHelper(this);
            List<MusicDatabaseHelper.Music> musicList = dbHelper.getAllMusic();
            runOnUiThread(() -> loadMusicList(musicList));
        });

        // 初始化ViewModel
         musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);



        // 监听进度条和按钮的操作
        setupObservers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initUI() {


        // 播放控件
        textView_totalDuration = findViewById(R.id.total_duration);
        textView_playingDuration = findViewById(R.id.playing_duration);
        btn_play_pause = findViewById(R.id.play_pause_button);
        btn_previous = findViewById(R.id.previous_button);
        btn_next = findViewById(R.id.next_button);
        btn_rewind = findViewById(R.id.rewind_button);
        btn_fast_forward = findViewById(R.id.fast_forward_button);
        btn_background_play = findViewById(R.id.background_play_button);
        btn_play_mode = findViewById(R.id.play_mode_button);
        SeekBar = findViewById(R.id.music_progress);

        // 元数据
        musicCover = findViewById(R.id.music_cover);
        songTitle = findViewById(R.id.song_title);
        artistName = findViewById(R.id.artist_name);
        lyricsScrollView = findViewById(R.id.lyrics_scroll_view);
        lyricsContainer = findViewById(R.id.lyrics_container);

        // 初始化主页侧栏
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);
        ImageButton musicListButton = findViewById(R.id.music_list_button);
        musicListButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        btn_play_pause.setOnClickListener(v -> musicViewModel.togglePlayPause());
        btn_next.setOnClickListener(v -> musicViewModel.playNext());
        btn_previous.setOnClickListener(v -> musicViewModel.playPrevious());
        btn_play_mode.setOnClickListener(v -> {
            musicViewModel.togglePlayMode(); // 切换播放模式
        });

        SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                musicViewModel.stopUpdatingPosition();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicViewModel.seekTo(seekBar.getProgress()); // 更新播放位置
                musicViewModel.startUpdatingPosition(); // 恢复自动更新
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicViewModel.seekTo(progress);
                }
            }
        });
    }

    // TODO: 看看能不能分开
    private void initButton(){
        ImageButton musicListButton = findViewById(R.id.music_list_button);
        musicListButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        btn_play_pause.setOnClickListener(v -> musicViewModel.togglePlayPause());

        SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                musicViewModel.stopUpdatingPosition(); // 暂停自动更新
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicViewModel.seekTo(seekBar.getProgress()); // 更新播放位置
                musicViewModel.startUpdatingPosition(); // 恢复自动更新
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicViewModel.seekTo(progress);
                }
            }
        });
    }


    private void setupObservers() {

        // 当前播放时间
        musicViewModel.getCurrentPosition().observe(this, position -> {
            SeekBar.setProgress(position);
            if (lyricsContainer != null) {
                updateCurrentLyrics(position);
            }
            textView_playingDuration.setText(MusicFormatConverter.formatDuration(position));
        });

        // 总时间
        musicViewModel.getDuration().observe(this, duration -> {
            SeekBar.setMax(duration);
            textView_totalDuration.setText(MusicFormatConverter.formatDuration(duration));
        });

        // 元数据
        musicViewModel.getMusicMetadata().observe(this, metadata -> {
            if (metadata != null) {
                if (metadata.getCoverImage() != null) {
                    musicCover.setImageBitmap(metadata.getCoverImage());
                } else {
                    musicCover.setImageResource(R.drawable.cover);
                }
                songTitle.setText(metadata.getTitle());
                artistName.setText(metadata.getArtist());
            }
        });

        // 观察播放状态
        musicViewModel.isPlaying().observe(this, isPlaying -> {
            if (isPlaying != null) {
                btn_play_pause.setImageResource(isPlaying ? R.drawable.pause : R.drawable.play);
            }
        });

        // 观察循环播放模式，
        // TODO：可能增加，不为三元表达式
        musicViewModel.getPlayMode().observe(this, mode -> {
            if (mode == MusicViewModel.PlayMode.LOOP_ONE) {
                btn_play_mode.setImageResource(R.drawable.single_loop);
            } else if (mode == MusicViewModel.PlayMode.LOOP_ALL) {
                btn_play_mode.setImageResource(R.drawable.list_loop);
            }
        });

        // 观察歌词变化
        musicViewModel.getLyricsMap().observe(this, lyricsMap -> {
            if (lyricsMap != null && !lyricsMap.isEmpty()) {
                updateLyrics(lyricsMap);
            } else {
                showNoLyricsMessage(); // 显示“无歌词”提示
            }
        });

        // 设置快进按钮长按监听器
        btn_fast_forward.setOnLongClickListener(v -> {
            startFastForward();
            return true;
        });

        // 设置快退按钮长按监听器
        btn_rewind.setOnLongClickListener(v -> {
            startRewind();
            return true;
        });

        // 设置按钮抬起监听器，用于停止倍速操作
        btn_fast_forward.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                stopFastForwardOrRewind();
            }
            return false;
        });

        btn_rewind.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                stopFastForwardOrRewind();
            }
            return false;
        });
    }

    private void startFastForward() {
        musicViewModel.startFastForward();
    }

    private void startRewind() {
        musicViewModel.startRewind();
    }

    private void stopFastForwardOrRewind() {
        musicViewModel.stopFastForwardOrRewind();
    }

    private void updateLyrics(Map<Long, String> lyricsMap) {
        if (lyricsContainer == null) return;

        lyricsContainer.removeAllViews(); // 清除现有歌词行

        if (lyricsMap != null && !lyricsMap.isEmpty()) {
            for (Map.Entry<Long, String> entry : lyricsMap.entrySet()) {
                TextView textView = new TextView(this);
                textView.setText(entry.getValue());
                textView.setTag(entry.getKey());
                textView.setTextColor(getResources().getColor(R.color.normal_color));
                textView.setTypeface(Typeface.DEFAULT);
                lyricsContainer.addView(textView);
            }
        } else {
            showNoLyricsMessage(); // 显示“无歌词”提示
        }
    }

    private void showNoLyricsMessage() {
        if (lyricsContainer == null) {
            return; // 确保在使用 lyricsContainer 之前它已被正确初始化
        }
        TextView noLyricsText = new TextView(this);
        noLyricsText.setText("无歌词");
        noLyricsText.setGravity(Gravity.CENTER);  // 将提示文本居中显示
        noLyricsText.setTextColor(getResources().getColor(R.color.normal_color));  // 设置文本颜色
        lyricsContainer.removeAllViews();
        lyricsContainer.addView(noLyricsText);
    }

    private void updateCurrentLyrics(long currentTime) {
        TextView previousTextView = null;
        boolean scrollToCurrent = true;

        for (int i = 0; i < lyricsContainer.getChildCount(); i++) {
            TextView textView = (TextView) lyricsContainer.getChildAt(i);
            Long timestamp = (Long) textView.getTag();

            if (timestamp == null) continue;

            if (currentTime >= timestamp) {
                if (previousTextView != null) {
                    previousTextView.setTextColor(getResources().getColor(R.color.normal_color)); // 恢复之前高亮的歌词
                    previousTextView.setTypeface(Typeface.DEFAULT);
                }

                textView.setTextColor(getResources().getColor(R.color.highlight_color)); // 高亮当前歌词
                textView.setTypeface(Typeface.DEFAULT_BOLD);

                // 仅在当前歌词发生变化时滚动
                if (!textView.equals(previousTextView)) {
                }

                previousTextView = textView;
            }
        }

        if (scrollToCurrent && previousTextView != null) {
            // 计算当前歌词相对于ScrollView的位置，使其居中
            int scrollTo = previousTextView.getTop() - (lyricsScrollView.getHeight() / 2 - previousTextView.getHeight() / 2);
            lyricsScrollView.smoothScrollTo(0, scrollTo);

        }
    }

    private void loadMusicFiles() {
        File musicDirectory = new File("/sdcard/Music");
        File[] musicFiles = musicDirectory.listFiles();

        if (musicFiles != null) {
            MusicDatabaseHelper dbHelper = new MusicDatabaseHelper(this);
            dbHelper.clearAllMusic(); // 每次加载前清空数据库，避免重复

            for (File file : musicFiles) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    try {
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(file.getAbsolutePath());

                        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                        if (title == null) title = file.getName();
                        if (artist == null) artist = "Unknown Artist";

                        // 在添加到数据库之前检查是否已经存在
                        if (!dbHelper.isMusicExists(file.getAbsolutePath())) {
                            dbHelper.addMusic(title, artist, file.getAbsolutePath());
                        }

                        retriever.release(); // 释放资源
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            runOnUiThread(() -> Toast.makeText(this, "未找到任何音乐文件", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadMusicList(List<MusicDatabaseHelper.Music> musicList) {
        RecyclerView musicListView = findViewById(R.id.music_list_container);
        musicListView.setLayoutManager(new LinearLayoutManager(this));

        musicViewModel.setMusicList(musicList);

        MusicListAdapter adapter = new MusicListAdapter(musicList, music -> {
            musicViewModel.playMusic(music.getPath());
            drawerLayout.closeDrawer(navigationView);
        });
        musicListView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(MusicActivity.this,
                Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicActivity.this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                    1);
        } else {
            loadMusicFiles();
            initUI();
        }
    }


}
