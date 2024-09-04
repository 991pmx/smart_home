package com.neusoft.testapplication.Device;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.neusoft.testapplication.R;
import com.neusoft.testapplication.Summary.MainActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ModeActivity extends AppCompatActivity {
    static int ac=DeviceActivity.getac();
    static int soc=DeviceActivity.getsoc();
    static int ap=DeviceActivity.getap();
    static int hum=DeviceActivity.gethum();
    static int bool=1;
    static int mode=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);

        TextView time = findViewById(R.id.time);
        TextView voice = findViewById(R.id.voicetext);
        // 获取当前时间
        Date date = new Date();
        // 创建一个SimpleDateFormat对象并设置格式
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm a", Locale.getDefault());
        // 使用SimpleDateFormat对象格式化Date对象
        String formattedDate = sdf.format(date);
        // 显示时间
        time.setText(formattedDate);

        //显示音量
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取媒体音量
        voice.setText(Integer.toString(currentVolume));

        //初始化当前开启设备数量
        InitDevice();

        //初始化当前模式
        ImageView image = findViewById(R.id.Mode_image);
        setmode(image);
        //初始化模式按钮
        InitModeButton();
        //获取四种模式组件
        ConstraintLayout mode_c = findViewById(R.id.mode_c);
        ConstraintLayout mode_l = findViewById(R.id.mode_l);
        ConstraintLayout mode_e = findViewById(R.id.mode_e);
        ConstraintLayout mode_s = findViewById(R.id.mode_s);
        ImageButton back = findViewById(R.id.Back);
        ImageButton backhome = findViewById(R.id.HomeButton);

        //进入设备列表
        ImageButton device = findViewById(R.id.Device_image);
        device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ModeActivity.this, DeviceActivity.class);
                startActivity(it);
            }
        });
        //监听返回首页键
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int num = ac * 1000 + soc * 100 + ap * 10 + hum;
                    //写入数据
                    File appDir = getFilesDir();
                    // 2. 创建文件对象
                    File file = new File(appDir, "device.txt");
                    try {
                        // 3. 打开文件输出流
                        FileOutputStream fos = openFileOutput("device.txt", MODE_PRIVATE);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                        // 4. 写入数据
                        String dataToWrite = String.valueOf(num);
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
                    Intent it = new Intent(ModeActivity.this, MainActivity.class);
                    startActivity(it);
                }
        });

        //监听返回桌面键
        backhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
            }
        });

        //监听在家模式按钮
        mode_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bool == 1) {
                    bool = 0;//锁
                    //更改设备相关信息
                    SetDeviceC();
                    //开启节能模式
                    TurnOnMode(mode_c);
                    ImageView scan=findViewById(R.id.scan_c);
                    TurnOn(scan);
                    //写入数据
                    File appDir = getFilesDir();
                    // 2. 创建文件对象
                    File file = new File(appDir, "device.txt");
                    try {
                        // 3. 打开文件输出流
                        FileOutputStream fos = openFileOutput("device.txt", MODE_PRIVATE);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                        // 4. 写入数据
                        String dataToWrite = "1111";
                        writer.write(dataToWrite);
                        // 5. 关闭流
                        writer.close();
                        fos.close();
                        // 数据已写入文件
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 处理异常
                    }
                    //其他模式关闭
                    TurnOffModeL(mode_l);
                    TurnOffModeE(mode_e);
                    TurnOffModeS(mode_s);
                    //调整模式按钮
                    ImageView image = findViewById(R.id.Mode_image);
                    image.setImageResource(R.drawable.home_on);
                }
            }
        });

        //监听离家模式按钮
        mode_l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bool == 1) {
                    bool = 0;//锁
                    //更改设备相关信息
                    SetDeviceL();
                    //开启离家模式
                    TurnOnMode(mode_l);
                    ImageView scan=findViewById(R.id.scan_l);
                    TurnOn(scan);
                    //写入数据
                    File appDir = getFilesDir();
                    // 2. 创建文件对象
                    File file = new File(appDir, "device.txt");
                    try {
                        // 3. 打开文件输出流
                        FileOutputStream fos = openFileOutput("device.txt", MODE_PRIVATE);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                        // 4. 写入数据
                        String dataToWrite = "0000";
                        writer.write(dataToWrite);
                        // 5. 关闭流
                        writer.close();
                        fos.close();
                        // 数据已写入文件
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 处理异常
                    }

                    //其他模式置0
                    //其他模式关闭
                    TurnOffModeC(mode_c);
                    TurnOffModeE(mode_e);
                    TurnOffModeS(mode_s);

                    //调整模式按钮
                    ImageView image = findViewById(R.id.Mode_image);
                    image.setImageResource(R.drawable.leave_on);
                }
            }
        });

        //监听节能模式按钮
        mode_e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bool == 1) {
                    bool = 0;
                    // 在此处处理点击事件
                    //更改设备相关信息
                    SetDeviceE();
                    //开启节能模式
                    TurnOnMode(mode_e);
                    ImageView scan=findViewById(R.id.scan_e);
                    TurnOn(scan);
                    //写入数据
                    File appDir = getFilesDir();
                    // 2. 创建文件对象
                    File file = new File(appDir, "device.txt");
                    try {
                        // 3. 打开文件输出流
                        FileOutputStream fos = openFileOutput("device.txt", MODE_PRIVATE);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                        // 4. 写入数据
                        String dataToWrite = "0101";
                        writer.write(dataToWrite);
                        // 5. 关闭流
                        writer.close();
                        fos.close();
                        // 数据已写入文件
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 处理异常
                    }

                    //其他模式关闭
                    TurnOffModeC(mode_c);
                    TurnOffModeS(mode_s);
                    TurnOffModeL(mode_l);

                    //调整模式按钮
                    ImageView image = findViewById(R.id.Mode_image);
                    image.setImageResource(R.drawable.energy_on);
                }
            }
        });

        //监听睡觉模式按钮
        mode_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bool == 1) {
                    bool = 0;
                    //更改设备相关信息
                    SetDeviceS();
                    //开启睡觉模式
                    TurnOnMode(mode_s);
                    ImageView scan=findViewById(R.id.scan_s);
                    TurnOn(scan);
                    //写入数据
                    File appDir = getFilesDir();
                    File file = new File(appDir, "device.txt");
                    try {
                        // 3. 打开文件输出流
                        FileOutputStream fos = openFileOutput("device.txt", MODE_PRIVATE);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                        // 4. 写入数据
                        String dataToWrite = "1100";
                        writer.write(dataToWrite);
                        // 5. 关闭流
                        writer.close();
                        fos.close();
                        // 数据已写入文件
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 处理异常
                    }
                    //其他模式关闭
                    TurnOffModeC(mode_c);
                    TurnOffModeE(mode_e);
                    TurnOffModeL(mode_l);

                    //调整模式按钮
                    ImageView image = findViewById(R.id.Mode_image);
                    image.setImageResource(R.drawable.sleep_on);
                }
            }
        });
    }
    public static int initmode()//初始化当前模式
    {
        if(DeviceActivity.getac()==1&&DeviceActivity.getsoc()==1
                &&DeviceActivity.getap()==1&&DeviceActivity.gethum()==1)
            mode=1;
        else if(DeviceActivity.getac()==0&&DeviceActivity.getsoc()==0
                &&DeviceActivity.getap()==0&&DeviceActivity.gethum()==0)
            mode=2;
        else if(DeviceActivity.getac()==0&&DeviceActivity.getsoc()==1
                &&DeviceActivity.getap()==0&&DeviceActivity.gethum()==1)
            mode=3;
        else if(DeviceActivity.getac()==1&&DeviceActivity.getsoc()==1
                &&DeviceActivity.getap()==0&&DeviceActivity.gethum()==0)
            mode=4;
        else
            mode=0;
        return mode;
    }
    //初始化模式按钮
    public void InitModeButton()
    {
        ImageView image;

        //获取四种模式组件
        ConstraintLayout mode_c = findViewById(R.id.mode_c);
        ConstraintLayout mode_l = findViewById(R.id.mode_l);
        ConstraintLayout mode_e = findViewById(R.id.mode_e);
        ConstraintLayout mode_s = findViewById(R.id.mode_s);
        //初始化在家模式的状态
        if (mode == 1) {
            image = findViewById(R.id.Home_image);
            image.setImageResource(R.drawable.home_on);
            mode_c.setBackgroundColor(Color.rgb(70, 130, 255));
        } else {
            image = findViewById(R.id.Home_image);
            image.setImageResource(R.drawable.home_off);
            mode_c.setBackgroundColor(Color.rgb(61, 61, 61));
        }
        //初始化离家模式的状态
        if (mode == 2) {
            image = findViewById(R.id.Leave_image);
            image.setImageResource(R.drawable.leave_on);
            mode_l.setBackgroundColor(Color.rgb(70, 130, 255));
        } else {
            image = findViewById(R.id.Leave_image);
            image.setImageResource(R.drawable.leave_off);
            mode_l.setBackgroundColor(Color.rgb(61, 61, 61));
        }
        //初始化节能模式的状态
        if (mode == 3) {
            image = findViewById(R.id.Energy_image);
            image.setImageResource(R.drawable.energy_on);
            mode_e.setBackgroundColor(Color.rgb(70, 130, 255));
        } else {
            image = findViewById(R.id.Energy_image);
            image.setImageResource(R.drawable.energy_off);
            mode_e.setBackgroundColor(Color.rgb(61, 61, 61));
        }
        //初始化睡觉模式的状态
        if (mode == 4) {
            image = findViewById(R.id.Sleep_image);
            image.setImageResource(R.drawable.sleep_on);
            mode_s.setBackgroundColor(Color.rgb(70, 130, 255));
        } else {
            image = findViewById(R.id.Sleep_image);
            image.setImageResource(R.drawable.sleep_off);
            mode_s.setBackgroundColor(Color.rgb(61, 61, 61));
        }
    }
    public void InitDevice()
    {
        ac=DeviceActivity.getac();
        soc=DeviceActivity.getsoc();
        ap=DeviceActivity.getap();
        hum=DeviceActivity.gethum();
        int num=ac+soc+ap+hum;
        String Num=String.valueOf(num)+" Device";
        TextView T;
        T=findViewById(R.id.num1);
        T.setText(Num);
        T=findViewById(R.id.num2);
        T.setText(Num);
        T=findViewById(R.id.num3);
        T.setText(Num);
        T=findViewById(R.id.num4);
        T.setText(Num);
    }
    public void SetDeviceC()
    {
        //更改设备相关信息
        ImageView image = findViewById(R.id.Home_image);
        image.setImageResource(R.drawable.home_on);
        DeviceActivity.setac(1);
        DeviceActivity.setsoc(1);
        DeviceActivity.setap(1);
        DeviceActivity.sethum(1);
        ac = soc = ap = hum = 1;
        mode = 1;
        String Num = "4 Device";
        TextView T;
        T = findViewById(R.id.num1);
        T.setText(Num);
        T = findViewById(R.id.num2);
        T.setText(Num);
        T = findViewById(R.id.num3);
        T.setText(Num);
        T = findViewById(R.id.num4);
        T.setText(Num);
    }
    public void SetDeviceL()
    {
        //更改设备相关信息
        ImageView image = findViewById(R.id.Leave_image);
        image.setImageResource(R.drawable.leave_on);
        DeviceActivity.setac(0);
        DeviceActivity.setsoc(0);
        DeviceActivity.setap(0);
        DeviceActivity.sethum(0);
        ac = soc = ap = hum = 0;
        mode = 2;
        String Num = "0 Device";
        TextView T;
        T = findViewById(R.id.num1);
        T.setText(Num);
        T = findViewById(R.id.num2);
        T.setText(Num);
        T = findViewById(R.id.num3);
        T.setText(Num);
        T = findViewById(R.id.num4);
        T.setText(Num);
    }
    public void SetDeviceE()
    {
        //更改设备相关信息
        ImageView image = findViewById(R.id.Energy_image);
        image.setImageResource(R.drawable.energy_on);
        DeviceActivity.setac(0);
        DeviceActivity.setsoc(1);
        DeviceActivity.setap(0);
        DeviceActivity.sethum(1);
        ac = ap = 0;
        soc = hum = 1;
        mode = 3;
        String Num = "2 Device";
        TextView T;
        T = findViewById(R.id.num1);
        T.setText(Num);
        T = findViewById(R.id.num2);
        T.setText(Num);
        T = findViewById(R.id.num3);
        T.setText(Num);
        T = findViewById(R.id.num4);
        T.setText(Num);
    }
    public void SetDeviceS()
    {
        //更改设备相关信息
        ImageView image = findViewById(R.id.Sleep_image);
        image.setImageResource(R.drawable.sleep_on);
        DeviceActivity.setac(1);
        DeviceActivity.setsoc(1);
        DeviceActivity.setap(0);
        DeviceActivity.sethum(0);
        ac = soc = 1;
        ap = hum = 0;
        mode = 4;
        String Num = "2 Device";
        TextView T;
        T = findViewById(R.id.num1);
        T.setText(Num);
        T = findViewById(R.id.num2);
        T.setText(Num);
        T = findViewById(R.id.num3);
        T.setText(Num);
        T = findViewById(R.id.num4);
        T.setText(Num);
    }
    public static void setmode(ImageView image)//设置Mode按钮
    {
        int i=ModeActivity.initmode();
        if(i==1)
        {
            image.setImageResource(R.drawable.home_on);
        }
        else if(i==2)
        {
            image.setImageResource(R.drawable.leave_on);
        }
        else if(i==3)
        {
            image.setImageResource(R.drawable.energy_on);
        }
        else if(i==4)
        {
            image.setImageResource(R.drawable.sleep_on);
        }
        else
        {
            image.setImageResource(R.drawable.mode_on);
        }
    }

    public static void TurnOnMode(ConstraintLayout mode)
    {
        //改变背景色
        // 创建一个值动画，从黑色到蓝色
        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(), // 用于计算颜色值
                0xFF3D3D3D, // 起始颜色
                0xFF4682FF  // 结束颜色
        );
        colorAnimation.setDuration(1250); // 设置动画持续时间
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 获取计算后的中间颜色值
                int animatedValue = (int) animation.getAnimatedValue();
                // 将颜色值设置为组件的背景
                mode.setBackgroundColor(animatedValue);
            }
        });
        colorAnimation.start();
    }
    public  void TurnOn(ImageView imageView)
    {
        imageView.setTranslationX(0f); // 设置初始位置\
        // 平移图片到x轴100像素的位置
        imageView.setVisibility(1);
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "translationX", 1850f);
        animator.setDuration(1300); // 设置动画持续时间，单位毫秒

        //监听动画结束后解锁
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator colorAnimation) {
                bool=1;
                super.onAnimationEnd(colorAnimation);
            }
        });
        animator.start(); // 开始动画

    }
    public void TurnOffModeC(ConstraintLayout mode)
    {
        ImageView image=findViewById(R.id.Home_image);
        image.setImageResource(R.drawable.home_off);
        mode.setBackgroundColor(Color.rgb(61, 61, 61));
    }
    public void TurnOffModeL(ConstraintLayout mode)
    {
        ImageView image=findViewById(R.id.Leave_image);
        image.setImageResource(R.drawable.leave_off);
        mode.setBackgroundColor(Color.rgb(61, 61, 61));
    }
    public void TurnOffModeE(ConstraintLayout mode)
    {
        ImageView image=findViewById(R.id.Energy_image);
        image.setImageResource(R.drawable.energy_off);
        mode.setBackgroundColor(Color.rgb(61, 61, 61));
    }
    public void TurnOffModeS(ConstraintLayout mode)
    {
        ImageView image=findViewById(R.id.Sleep_image);
        image.setImageResource(R.drawable.sleep_off);
        mode.setBackgroundColor(Color.rgb(61, 61, 61));
    }

    public static int getmode()
    {
        return mode;
    }
}