package com.neusoft.testapplication.Device;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.neusoft.testapplication.R;
import com.neusoft.testapplication.Summary.MainActivity;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DeviceActivity extends AppCompatActivity {
    static int ac;
    static int soc;
    static int ap;
    static int hum;
    static int boolac=1;
    static int boolsoc=1;
    static int boolap=1;
    static int boolhum=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

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

        // 读取设备数据
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("device.txt")))) {
            String line;
            line = reader.readLine();
            String state= line;
            //分析并显示
            int number=Integer.parseInt(state);
            ac=number/1000;
            soc=number/100%10;
            ap=number%100/10;
            hum=number%10;

        } catch (IOException e) {
            e.printStackTrace();
            // 处理异常
        }

        //获取四种设备组件
        ConstraintLayout state_AC = findViewById(R.id.device_AC);
        ConstraintLayout state_Soc = findViewById(R.id.device_Soc);
        ConstraintLayout state_AP = findViewById(R.id.device_AP);
        ConstraintLayout state_Hum = findViewById(R.id.device_Hum);
        ImageButton mode = findViewById(R.id.Mode_image);
        ImageButton back = findViewById(R.id.Back);
        ImageButton backhome = findViewById(R.id.HomeButton);

        //初始化Mode
        setmode();
        ImageView image;
        //初始化AC的状态
        if(ac ==1) {
            TextView TEXT = findViewById(R.id.AC_state);
            TEXT.setText("On");
            image = findViewById(R.id.AC_image);
            image.setImageResource(R.drawable.air_conditioner_on);
            state_AC.setBackgroundColor(Color.rgb(70,130,255));
        }
        else {
            TextView TEXT = findViewById(R.id.AC_state);
            TEXT.setText("Off");
            image = findViewById(R.id.AC_image);
            image.setImageResource(R.drawable.air_conditioner_off);
            state_AC.setBackgroundColor(Color.rgb(61,61,61));
        }
        //初始化Soc的状态
        if(soc ==1) {
            TextView TEXT = findViewById(R.id.Soc_state);
            TEXT.setText("On");
            image = findViewById(R.id.Soc_image);
            image.setImageResource(R.drawable.socket_on);
            state_Soc.setBackgroundColor(Color.rgb(70,130,255));
        }
        else {
            TextView TEXT = findViewById(R.id.Soc_state);
            TEXT.setText("Off");
            image = findViewById(R.id.Soc_image);
            image.setImageResource(R.drawable.socket_off);
            state_Soc.setBackgroundColor(Color.rgb(61,61,61));
        }
        //初始化AP的状态
        if(ap ==1) {
            TextView TEXT = findViewById(R.id.AP_state);
            TEXT.setText("On");
            image = findViewById(R.id.AP_image);
            image.setImageResource(R.drawable.ap_on);
            state_AP.setBackgroundColor(Color.rgb(70,130,255));
        }
        else {
            TextView TEXT = findViewById(R.id.AP_state);
            TEXT.setText("Off");
            image = findViewById(R.id.AP_image);
            image.setImageResource(R.drawable.ap_off);
            state_AP.setBackgroundColor(Color.rgb(61,61,61));
        }
        //初始化Hum的状态
        if(hum ==1) {
            TextView TEXT = findViewById(R.id.Hum_state);
            TEXT.setText("On");
            image = findViewById(R.id.Hum_image);
            image.setImageResource(R.drawable.humidifier_on);
            state_Hum.setBackgroundColor(Color.rgb(70,130,255));
        }
        else {
            TextView TEXT = findViewById(R.id.Hum_state);
            TEXT.setText("Off");
            image = findViewById(R.id.Hum_image);
            image.setImageResource(R.drawable.humidifier_off);
            state_Hum.setBackgroundColor(Color.rgb(61,61,61));
        }

        //监听返回首页键
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num=ac*1000+soc*100+ap*10+hum;
                //写入数据
                File appDir = getFilesDir();
                // 2. 创建文件对象
                File file = new File(appDir, "device.txt");
                try {
                    // 3. 打开文件输出流
                    FileOutputStream fos = openFileOutput("device.txt", MODE_PRIVATE);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                    // 4. 写入数据
                    String dataToWrite = String.valueOf(num);;
                    writer.write(dataToWrite);
                    // 5. 关闭流
                    writer.close();
                    fos.close();
                    // 数据已写入文件
                } catch (IOException e) {
                    e.printStackTrace();
                    // 处理异常
                }
                Intent it=new Intent(DeviceActivity.this, MainActivity.class);
                startActivity(it);
            }
        });

        //监听返回桌面键
        backhome.setOnClickListener(new View.OnClickListener() {
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
                moveTaskToBack(true);
            }
        });

        //监听AC的点击
        state_AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boolac == 1) {
                    boolac=0;
                    // 在此处处理点击事件
                    if (ac == 0) {
                        TextView TEXT = findViewById(R.id.AC_state);
                        ImageView image = findViewById(R.id.AC_image);
                        image.setImageResource(R.drawable.air_conditioner_on);
                        ac = 1;
                        TEXT.setText("Turning On");
                        //开启设备
                        TurnOn(TEXT,state_AC,1);
                        //更新模式按钮
                        setmode();
                    } else {
                        TextView TEXT = findViewById(R.id.AC_state);
                        TEXT.setText("Turning Off");
                        ImageView image = findViewById(R.id.AC_image);
                        image.setImageResource(R.drawable.air_conditioner_off);
                        ac = 0;
                        //关闭设备
                        TurnOff(TEXT,state_AC,1);
                        //更新模式按钮
                        setmode();
                    }
                }
            }
        });

        //监听Soc的点击
        state_Soc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boolsoc == 1) {
                    boolsoc=0;
                    if (soc == 0) {
                        TextView TEXT = findViewById(R.id.Soc_state);
                        TEXT.setText("Turning On");
                        ImageView image = findViewById(R.id.Soc_image);
                        image.setImageResource(R.drawable.socket_on);
                        soc = 1;
                        //开启设备
                        TurnOn(TEXT,state_Soc,2);
                        //更新模式按钮
                        setmode();
                    } else {
                        TextView TEXT = findViewById(R.id.Soc_state);
                        TEXT.setText("Turning Off");
                        ImageView image = findViewById(R.id.Soc_image);
                        image.setImageResource(R.drawable.socket_off);
                        soc = 0;
                        //关闭设备
                        TurnOff(TEXT,state_Soc,2);
                        //更新模式按钮
                        setmode();
                    }
                }
            }
        });

        //监听AP的点击
        state_AP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boolap == 1) {
                    boolap=0;
                    // 在此处处理点击事件
                    if (ap == 0) {
                        TextView TEXT = findViewById(R.id.AP_state);
                        TEXT.setText("Turning On");
                        ImageView image = findViewById(R.id.AP_image);
                        image.setImageResource(R.drawable.ap_on);
                        ap = 1;
                        //开启设备
                        TurnOn(TEXT,state_AP,3);
                        //更新模式按钮
                        setmode();
                    } else {
                        TextView TEXT = findViewById(R.id.AP_state);
                        TEXT.setText("Turning Off");
                        ImageView image = findViewById(R.id.AP_image);
                        image.setImageResource(R.drawable.ap_off);
                        ap = 0;
                        //关闭设备
                        TurnOff(TEXT,state_AP,3);
                        //更新模式按钮
                        setmode();
                    }
                }
            }
        });

        //监听Hum的点击
        state_Hum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boolhum == 1) {
                    boolhum = 0;
                    // 在此处处理点击事件
                    if (hum == 0) {
                        TextView TEXT = findViewById(R.id.Hum_state);
                        TEXT.setText("Turning On");
                        ImageView image = findViewById(R.id.Hum_image);
                        image.setImageResource(R.drawable.humidifier_on);
                        hum = 1;
                        //开启设备
                        TurnOn(TEXT,state_Hum,4);
                        //更新模式按钮
                        setmode();
                    } else {
                        TextView TEXT = findViewById(R.id.Hum_state);
                        TEXT.setText("Turning Off");
                        ImageView image = findViewById(R.id.Hum_image);
                        image.setImageResource(R.drawable.humidifier_off);
                        hum = 0;
                        //关闭设备
                        TurnOff(TEXT,state_Hum,4);
                        //更新模式按钮
                        setmode();
                    }
                }
            }
        });

        //监听Mode点击
        mode.setOnClickListener(new View.OnClickListener() {
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
                Intent it=new Intent(DeviceActivity.this, ModeActivity.class);
                startActivity(it);
            }
        });
    }
    public static int getac()
    {
        return ac;
    }
    public static int getsoc()
    {
        return soc;
    }
    public static int getap()
    {
        return ap;
    }
    public static int gethum()
    {
        return hum;
    }
    public static void setac(int x)
    {
        ac=x;
    }
    public static void setsoc(int x)
    {
        soc=x;
    }
    public static void setap(int x)
    {
        ap=x;
    }
    public static void sethum(int x)
    {
        hum=x;
    }

    //关闭设备
    public static void TurnOff(TextView TEXT,ConstraintLayout state,int i)
    {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(), // 用于计算颜色值
                0xFF4682FF, // 起始颜色
                0xFF3D3D3D// 结束颜色
        );
        colorAnimation.setDuration(500); // 设置动画持续时间
        //监听动画结束后解锁
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator colorAnimation) {
                if(i==1) {
                    boolac = 1;
                }
                else if(i==2)
                {
                    boolsoc=1;
                }
                else if(i==3)
                {
                    boolap=1;
                }
                else
                    boolhum=1;
                TEXT.setText("Off");
                super.onAnimationEnd(colorAnimation);
            }
        });
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                state.setBackgroundColor(animatedValue);
            }
        });
        colorAnimation.start();
    }

    //开启设备
    public static void TurnOn(TextView TEXT,ConstraintLayout state,int i)
    {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(), // 用于计算颜色值
                0xFF3D3D3D, // 起始颜色
                0xFF4682FF// 结束颜色
        );
        colorAnimation.setDuration(1000); // 设置动画持续时间
        //监听动画结束后解锁
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator colorAnimation) {
                if(i==1) {
                    boolac = 1;
                }
                else if(i==2)
                {
                    boolsoc=1;
                }
                else if(i==3)
                {
                    boolap=1;
                }
                else
                    boolhum=1;
                TEXT.setText("On");
                super.onAnimationEnd(colorAnimation);
            }
        });
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                state.setBackgroundColor(animatedValue);
            }
        });
        colorAnimation.start();
    }
    public void setmode() {
        ImageView image=findViewById(R.id.Mode_image);
        TextView Text =findViewById(R.id.ModeText);
        int i = ModeActivity.initmode();
            switch (i) {
                case 1:
                    image.setImageResource(R.drawable.home_off);
                    Text.setText("Current Mode:Coming Home");
                    break;
                case 2:
                    image.setImageResource(R.drawable.leave_off);
                    Text.setText("Current Mode:Leaving Home");
                    break;
                case 3:
                    image.setImageResource(R.drawable.energy_off);
                    Text.setText("Current Mode:Energy Saving");
                    break;
                case 4:
                    image.setImageResource(R.drawable.sleep_off);
                    Text.setText("Current Mode:Sleep Mode");
                    break;
                default:
                    image.setImageResource(R.drawable.mode_off);
                    Text.setText("Current Mode:Not In Any Mode");
        }
    }
}