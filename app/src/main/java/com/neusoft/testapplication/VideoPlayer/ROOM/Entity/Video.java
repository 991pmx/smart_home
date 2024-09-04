package com.neusoft.testapplication.VideoPlayer.ROOM.Entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Video implements Parcelable {
    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
    private String thumbnailData;//缩略图
    //    视频种类
    private int kind;
    //    宽度
    private long width;
    //    长度
    private long height;

    @PrimaryKey
    private int id;
    //    大小
    private long size;
    //    作者
    private String artist;
    //    描述
    private String description;
    //    上次播放位置
    private int bookmark;
    //    标题
    private String title;
    //    总时长
    private long duration;
    //视频地址
    private String data;

    public Video() {
    }

    protected Video(Parcel in) {
        thumbnailData = in.readString();
        kind = in.readInt();
        width = in.readLong();
        height = in.readLong();
        id = in.readInt();
        size = in.readLong();
        artist = in.readString();
        description = in.readString();
        bookmark = in.readInt();
        title = in.readString();
        duration = in.readLong();
        data = in.readString();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getThumbnailData() {
        return thumbnailData;
    }

    public void setThumbnailData(String thumbnailData) {
        this.thumbnailData = thumbnailData;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBookmark() {
        return bookmark;
    }

    public void setBookmark(int bookmark) {
        this.bookmark = bookmark;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Video{" +
                "thumbnailData='" + thumbnailData + '\'' +
                ", kind=" + kind +
                ", width=" + width +
                ", height=" + height +
                ", id=" + id +
                ", size=" + size +
                ", artist='" + artist + '\'' +
                ", description='" + description + '\'' +
                ", bookmark=" + bookmark +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(thumbnailData);
        parcel.writeInt(kind);
        parcel.writeLong(width);
        parcel.writeLong(height);
        parcel.writeInt(id);
        parcel.writeLong(size);
        parcel.writeString(artist);
        parcel.writeString(description);
        parcel.writeInt(bookmark);
        parcel.writeString(title);
        parcel.writeLong(duration);
        parcel.writeString(data);
    }
}






