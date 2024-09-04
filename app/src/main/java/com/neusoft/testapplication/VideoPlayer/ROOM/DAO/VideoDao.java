package com.neusoft.testapplication.VideoPlayer.ROOM.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.neusoft.testapplication.VideoPlayer.ROOM.Entity.Video;

import java.util.List;

@Dao
public interface VideoDao {
    @Query("SELECT * FROM Video")
    List<Video> getAllVideos();

    @Query("SELECT * FROM Video WHERE id = :id")
    Video getVideoById(long id);

    @Update
    void updateVideo(Video video);

    @Query("DELETE FROM Video WHERE id = :id")
    void deleteVideo(int id);

    // 插入多个视频（List操作）
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addVideos(List<Video> videos);

    // 删除所有视频
    @Query("DELETE FROM Video")
    void deleteVideos();
}