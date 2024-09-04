package com.neusoft.testapplication.VideoPlayer.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import androidx.annotation.NonNull;

public class CustomVideoView extends VideoView {
    private static final String TAG = "Video";
    MyGestureListener myGestureListener;
    private GestureDetector gestureDetector;
    private boolean issmall;

    public CustomVideoView(Context context) {
        super(context);
        init();
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setIssmall(boolean issmall) {
        this.issmall = issmall;
    }

    public GestureDetector getGestureDetector() {
        return gestureDetector;
    }

    public void setMyGestureListener(MyGestureListener myGestureListener) {
        this.myGestureListener = myGestureListener;
    }

    private void init() {
        GestureListener listener = new GestureListener();
        gestureDetector = new GestureDetector(getContext(), listener);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (issmall) {
                    return true;
                } else {
                    return gestureDetector.onTouchEvent(event);
                }
            }
        });

    }

    public interface MyGestureListener {
        void playorpause();

        void isbtnVisible();

        void adjustBrightness(float Brightness);

        void adjustVolume(float Volume);

    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHalfWidth = screenWidth / 2;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            // 计算滑动角度
            double angle = Math.abs(e1.getX() - e2.getX()) / Math.abs(e1.getY() - e2.getY());

            // 检查角度是否在+-30度内，并判断Y轴距离足够远以确认是垂直滑动
            if (Math.abs(angle) <= 30 && Math.abs(e2.getY() - e1.getY()) > 100) {
                if (e2.getX() < screenHalfWidth) {
                    Log.d(TAG, "onFling: " + (e1.getY() - e2.getY()) / screenHeight);
                    myGestureListener.adjustBrightness((e1.getY() - e2.getY()) / screenHeight);
                } else if (e2.getX() > screenHalfWidth) {
                    myGestureListener.adjustVolume((e1.getY() - e2.getY()) / screenHeight);
                }
                return true;

            }
            return false;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
//            一定要返回ture，否则不会调用其他函数
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            myGestureListener.playorpause();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed: ");
            myGestureListener.isbtnVisible();
            return super.onSingleTapConfirmed(e);
        }
    }
}
