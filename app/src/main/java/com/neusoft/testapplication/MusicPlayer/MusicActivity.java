package com.neusoft.testapplication.MusicPlayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.neusoft.testapplication.MusicPlayer.adapter.MusicListAdapter;
import com.neusoft.testapplication.MusicPlayer.utils.MusicDatabaseHelper;
import com.neusoft.testapplication.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    private MusicPlayerController musicPlayerController; // 控制音乐播放和进度条的控制器
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView musicCover;
    private TextView songTitle;
    private TextView artistName;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        // 初始化 DrawerLayout 和 NavigationView
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);
        ImageButton musicListButton = findViewById(R.id.music_list_button);
        musicListButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // 动态申请读取音频文件权限
        if (ContextCompat.checkSelfPermission(MusicActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } else {
            // 异步操作
            handler.post(() -> {
                try {
                    loadMusicFiles();
                    setupUI();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicPlayerController != null) {
            musicPlayerController.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 异步操作
                handler.post(() -> {
                    try {
                        loadMusicFiles();
                        setupUI();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "读取音频文件的权限被拒绝，无法访问音乐文件", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void setupUI() throws IOException {
        SeekBar musicProgress = findViewById(R.id.music_progress);
        ImageButton btn_play_pause = findViewById(R.id.play_pause_button);
        TextView totalDurationTextView = findViewById(R.id.total_duration);
        TextView playingDurationTextView = findViewById(R.id.playing_duration);
        musicCover = findViewById(R.id.music_cover);
        songTitle = findViewById(R.id.song_title);
        artistName = findViewById(R.id.artist_name);

        // 初始化 MusicPlayerController
        musicPlayerController = new MusicPlayerController(this, musicProgress, btn_play_pause,
                totalDurationTextView, playingDurationTextView);

        // 异步加载音乐列表并初始化UI
        handler.post(() -> {
            MusicDatabaseHelper dbHelper = new MusicDatabaseHelper(this);
            List<MusicDatabaseHelper.Music> musicList = dbHelper.getAllMusic();
            runOnUiThread(() -> loadMusicList(musicList));
        });

        // 使用默认的封面和信息，直到选择音乐
        setDefaultMetadata();
    }

    private void setDefaultMetadata() {
        musicCover.setImageResource(R.drawable.cover);
        songTitle.setText("默认标题");
        artistName.setText("默认艺人");
    }

    private void loadMusicList(List<MusicDatabaseHelper.Music> musicList) {
        RecyclerView musicListView = findViewById(R.id.music_list_container);
        musicListView.setLayoutManager(new LinearLayoutManager(this));
        MusicListAdapter adapter = new MusicListAdapter(musicList, music -> {
            playSelectedMusic(music.getPath());
            drawerLayout.closeDrawer(navigationView);
        });
        musicListView.setAdapter(adapter);
    }

    private void playSelectedMusic(String path) {
        // 更新元数据
        updateMetadata(path);
        // 播放音乐
        musicPlayerController.playMusic(path);
    }

    private void updateMetadata(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        // 获取并显示封面图片
        byte[] coverData = retriever.getEmbeddedPicture();
        if (coverData != null) {
            musicCover.setImageBitmap(BitmapFactory.decodeByteArray(coverData, 0, coverData.length));
        } else {
            musicCover.setImageResource(R.drawable.cover);
        }

        // 获取并显示标题
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        songTitle.setText(title != null ? title : new File(path).getName());

        // 获取并显示艺人信息
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        artistName.setText(artist != null ? artist : "未知艺人");

        try {
            retriever.release(); // 释放资源
        } catch (IOException e) {
            throw new RuntimeException(e);
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

}
