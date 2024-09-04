package com.neusoft.testapplication.MusicPlayer.viewmodel.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.IOException;

public class MusicMetadataRetriever {

    public static class MusicMetadata {
        private final String title;
        private final String artist;
        private final Bitmap coverImage;

        public MusicMetadata(String title, String artist, Bitmap coverImage) {
            this.title = title;
            this.artist = artist;
            this.coverImage = coverImage;
        }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }

        public Bitmap getCoverImage() {
            return coverImage;
        }
    }

    public static MusicMetadata retrieveMetadata(String path) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        // 获取并返回标题、艺人信息和封面图片
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        byte[] coverData = retriever.getEmbeddedPicture();
        Bitmap coverImage = null;
        if (coverData != null) {
            coverImage = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
        }

        retriever.release();

        // 如果没有标题，使用文件名代替
        if (title == null) {
            title = new File(path).getName();
        }

        // 如果没有艺人信息，使用“未知艺人”代替
        if (artist == null) {
            artist = "Unknown Artist";
        }

        return new MusicMetadata(title, artist, coverImage);
    }
}
