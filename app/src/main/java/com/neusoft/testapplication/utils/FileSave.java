package com.neusoft.testapplication.utils;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.neusoft.testapplication.Device.DeviceActivity;
import com.neusoft.testapplication.Device.ModeActivity;
import com.neusoft.testapplication.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileSave {
    private static final String TAG = "Video";

    public static void writefile(Context context, String message, String path){
        //写入数据
        File appDir = context.getFilesDir();
        // 2. 创建文件对象
        File file = new File(appDir, path);
        try {
            // 3. 打开文件输出流
            FileOutputStream fos = context.openFileOutput(path, context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            // 4. 写入数据
            String dataToWrite = message;
            ;
            writer.write(dataToWrite);
            // 5. 关闭流
            writer.close();
            fos.close();
            // 数据已写入文件
        } catch (IOException e) {
            e.printStackTrace();
            // 处理异常
        }
    }
    public static String readVideoFile(Context context){

        String path="Video.txt";
        String Title;
        String Bookmark;
        String mode;

        File files = new File(context.getFilesDir(), path);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput(path)))) {

            // 读取第一行
            Title = reader.readLine();

            // 读取第二行
            mode = reader.readLine();
            // 关闭reader
            reader.close();
            Log.d(TAG, "readVideoFile: "+Title+"  "+mode);

            return Title  + "," + mode; // 使用逗号作为分隔符
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "readVideoFile: "+e.toString() );
        }
        return null;
    }

    public static void writeVideoFile(Context context, String title, int mode) {
        String path = "Video.txt";
        File file = new File(context.getFilesDir(), path);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(path, Context.MODE_PRIVATE)))) {
            writer.write(title);
            writer.newLine();

            writer.write(Integer.toString(mode));
            // 注意：这里不需要writer.newLine()，因为通常是文件的最后一行
            Log.d(TAG, "writeVideoFile: "+title+"  "+mode);

            // 关闭writer（但在这里是自动的，因为使用了try-with-resources）
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
