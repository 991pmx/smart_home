package com.neusoft.testapplication.MusicPlayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.widget.TextView;

import com.neusoft.testapplication.R;

import java.io.File;
import java.io.IOException;

/**
 * MusicMetadataRetriever 类负责提取音频文件的元数据，并将其绑定到 UI 控件上。
 */
public class MusicMetadataRetriever {

    private Context context;
    private ImageView coverImageView; // 封面图片的 ImageView
    private TextView titleTextView; // 标题的 TextView
    private TextView artistTextView; // 艺人信息的 TextView

    /**
     * 构造函数，初始化 MusicMetadataRetriever。
     * @param context 上下文，用于加载资源
     * @param coverImageView 用于显示封面图片的 ImageView
     * @param titleTextView 用于显示标题的 TextView
     * @param artistTextView 用于显示艺人信息的 TextView
     */
    public MusicMetadataRetriever(Context context, ImageView coverImageView,
                                  TextView titleTextView, TextView artistTextView) {
        this.context = context;
        this.coverImageView = coverImageView;
        this.titleTextView = titleTextView;
        this.artistTextView = artistTextView;
    }

    /**
     * 提取音频文件的元数据并绑定到 UI 控件上。
     * @param file 音频文件
     */
    public void retrieveMetadata(File file) throws IOException {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        try {
            metadataRetriever.setDataSource(file.getAbsolutePath());

            // 获取封面图片
            byte[] coverData = metadataRetriever.getEmbeddedPicture();
            if (coverData != null) {
                Bitmap coverBitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
                coverImageView.setImageBitmap(coverBitmap);
            } else {
                coverImageView.setImageResource(R.drawable.cover); // 使用默认封面
            }

            // 获取标题
            String title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (title != null) {
                titleTextView.setText(title);
            } else {
                titleTextView.setText(file.getName()); // 使用文件名作为标题
            }

            // 获取艺人信息
            String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (artist != null) {
                artistTextView.setText(artist);
            } else {
                artistTextView.setText("未知艺人"); // 默认显示"未知艺人"
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常时使用默认信息
            coverImageView.setImageResource(R.drawable.cover);
            titleTextView.setText(file.getName());
            artistTextView.setText("未知艺人");
        } finally {
            metadataRetriever.release();
        }
    }
}

