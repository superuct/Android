package com.me.assignment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.sum.slike.SuperLikeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class VideoActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    protected TextView tv_start;
    protected TextView tv_end;
    protected TextView user_name;
    protected SeekBar seekBar;
    private Timer timer;//定时器
    private boolean isSeekbarChaning;//互斥变量，防止进度条和定时器冲突。

    private String VideoUrl, UserName;

    private int clickNum = 0;
    private Handler handler = new Handler();
    SuperLikeLayout superLikeLayout;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        getSupportActionBar().hide();

        surfaceView = findViewById(R.id.sv);
        tv_start = (TextView)findViewById(R.id.curr_time);
        tv_end = (TextView)findViewById(R.id.duration);
        seekBar = (SeekBar)findViewById(R.id.seekbar);
        user_name = (TextView)findViewById(R.id.user_name);

        superLikeLayout = findViewById(R.id.super_like_layout);
        superLikeLayout.setProvider(BitmapProviderFactory.getHDProvider(this));

        Intent intent = getIntent();
        UserName = intent.getStringExtra("user_name");
        VideoUrl = intent.getStringExtra("video_url");

        initView();//初始化进度条
        initMediaPlayer();//初始化MediaPlayer

        user_name.setText(UserName);


        Bitmap bitmap = getThumb(VideoUrl);
        if (null != bitmap)
        {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int pixel = bitmap.getPixel(w/2, h/10*9);

//            Log.i("TAG", String.valueOf(w)+"x"+String.valueOf(h));
            //获取颜色
            int sA = Color.alpha(pixel);
            int sR = Color.red(pixel);
            int sG = Color.green(pixel);
            int sB = Color.blue(pixel);
            sR = 255 - sR;
            sG = 255 - sG;
            sB = 255 - sB;

            int sPixel = ((sA & 0xff) << 24 | (sR & 0xff) << 16 | (sG & 0xff) << 8 | sB & 0xff);

//            Log.i("TAG", "【颜色值】 #" + String.valueOf(Integer.toHexString(pixel)));
            user_name.setTextColor(sPixel);
            tv_start.setTextColor(sPixel);
            tv_end.setTextColor(sPixel);

            bitmap.recycle();
        }


//        int color = surfaceView.getSolidColor();
//        Log.i("TAG", "onCreate: "+String.valueOf(color));

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNum++;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (clickNum == 1) {
                            if(!mediaPlayer.isPlaying()){
                                startPlay();
                            }else{
                                mediaPlayer.pause();//暂停播放
                            }
//                            Log.d("TAG:", "btn is clicked!");
                        }else if(clickNum==2){
                            int x = (int)(v.getX() + v.getWidth() / 2);
                            int y = (int)(v.getY() + v.getHeight() / 2);
                            superLikeLayout.launch(x, y);
//                            Log.d("TAG:", "btn is doubleClicked!");
                        }
                        //防止handler引起的内存泄漏
                        handler.removeCallbacksAndMessages(null);
                        clickNum = 0;
                    }
                },200);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startPlay();
            }
        });
    }

    public static Bitmap getThumb(String path) {
        Bitmap bitmap = null;
//MediaMetadataRetriever 是android中定义好的一个类，提供了统一
//的接口，用于从输入的媒体文件中取得帧和元数据；
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
//根据文件路径获取缩略图
            retriever.setDataSource(path, new HashMap<String, String>());
//获得第一帧图片
            bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_NEXT_SYNC);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    /*
     * 初始化
     * */
    private void initView(){

        //绑定监听器，监听拖动到指定位置
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int position = mediaPlayer.getCurrentPosition();//获取当前播放的位置
                tv_start.setText(calculateTime(position / 1000));//开始时间
//                tv_end.setText(calculateTime(duration / 1000));//总时长
            }
            /*
             * 通知用户已经开始一个触摸拖动手势。
             * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning = true;
            }
            /*
             * 当手停止拖动进度条时执行该方法
             * 首先获取拖拽进度
             * 将进度对应设置给MediaPlayer
             * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning = false;
                mediaPlayer.seekTo(seekBar.getProgress());//在当前位置播放
                tv_start.setText(calculateTime(mediaPlayer.getCurrentPosition() / 1000));
            }
        });

    }

    //计算播放时间
    public String calculateTime(int time){
        int minute;
        int second;
        if(time >= 60){
            minute = time / 60;
            second = time % 60;
            //分钟再0~9
            if(minute >= 0 && minute < 10){
                //判断秒
                if(second >= 0 && second < 10){
                    return "0"+minute+":"+"0"+second;
                }else {
                    return "0"+minute+":"+second;
                }
            }else {
                //分钟大于10再判断秒
                if(second >= 0 && second < 10){
                    return minute+":"+"0"+second;
                }else {
                    return minute+":"+second;
                }
            }
        }else if(time < 60){
            second = time;
            if(second >= 0 && second < 10){
                return "00:"+"0"+second;
            }else {
                return "00:"+ second;
            }
        }
        return null;
    }
    /*
     * 初始化MediaPlayer
     * */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initMediaPlayer(){
        try {
            mediaPlayer.setDataSource(VideoUrl);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    mediaPlayer.setDisplay(surfaceHolder);
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

                }
            });
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 自动播放
                    startPlay();
//                    Log.i("TAG", "onPrepared: play");
                }
            });
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    System.out.println(percent);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        int duration = mediaPlayer.getDuration();
//        Log.i("TAG", "initMediaPlayer: "+ Integer.toString(duration));
        int position = mediaPlayer.getCurrentPosition();
//        Log.i("TAG", "initMediaPlayer: "+ Integer.toString(position));
        tv_start.setText(calculateTime(position / 1000));
        tv_end.setText(" / "+calculateTime(duration / 1000));
//        Log.i("TAG", "initMediaPlayer: "+calculateTime(duration / 1000));
    }
    //使用时弹出提示框
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initView();
                    initMediaPlayer();

                }else {
                    Toast.makeText(this,"拒绝权限将无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public void startPlay(){
        mediaPlayer.start();//开始播放
//        mediaPlayer.setLooping(true);
        int duration = mediaPlayer.getDuration();//获取音乐总时间
//                    Log.i("TAG", "onClick: Play");
        seekBar.setMax(duration);//将音乐总时间设置为Seekbar的最大值
//                    Log.i("TAG", "onClick: Play");
        if(timer!=null){
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isSeekbarChaning){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
            }
        },0,50);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(timer!=null){
            timer.cancel();
        }
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}