package com.neusoft.testapplication.VideoPlayer.ROOM;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.neusoft.testapplication.VideoPlayer.ROOM.DAO.VideoDao;
import com.neusoft.testapplication.VideoPlayer.ROOM.Entity.Video;

@Database(entities = {Video.class}, version = 2, exportSchema = false)
public abstract class VideoDatabase extends RoomDatabase {

    //数据库名字
    private static final String DATABASE_NAME = "Video";
    //单例模式
    private static VideoDatabase databaseInstance;

    public static synchronized VideoDatabase getInstance(Context context) {
        if (databaseInstance == null) {
            databaseInstance = Room
                    .databaseBuilder(context.getApplicationContext(), VideoDatabase.class, DATABASE_NAME)
                    .build();


        }
        return databaseInstance;
    }

    public abstract VideoDao videoDao();

}


