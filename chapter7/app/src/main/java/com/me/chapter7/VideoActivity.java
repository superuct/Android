package com.me.chapter7;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.transition.Hold;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    protected TextView tv_start;
    protected TextView tv_end;
    protected SeekBar seekBar;
    private Timer timer;//定时器
    private boolean isSeekbarChaning;//互斥变量，防止进度条和定时器冲突。
    private Button play;
    private Button pause;
    private Button stop;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        surfaceView = findViewById(R.id.sv);

        play = (Button)findViewById(R.id.play);
        pause = (Button)findViewById(R.id.pause);
        stop = (Button)findViewById(R.id.replay);

        tv_start = (TextView)findViewById(R.id.curr);
        tv_end = (TextView)findViewById(R.id.duration);
        seekBar = (SeekBar)findViewById(R.id.seekbar);


        initView();//初始化进度条
        initMediaPlayer();//初始化MediaPlayer

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //变成横屏了
//            setVideoParams(mediaPlayer, true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //变成竖屏了
//            setVideoParams(mediaPlayer, false);
        }
    }



    /*
     * 初始化
     * */
    private void initView(){

        //绑定监听器，监听拖动到指定位置
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int duration = mediaPlayer.getDuration();//获取音乐总时长
                int position = mediaPlayer.getCurrentPosition();//获取当前播放的位置
//                Log.i("TAG", "initMediaPlayer: "+ Integer.toString(duration));
//                Log.i("TAG", "initMediaPlayer: "+ Integer.toString(position));
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
            mediaPlayer.setDataSource(getResources().openRawResourceFd(R.raw.big_buck_bunny));
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
//                    mediaPlayer.setLooping(true);
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
        tv_end.setText(calculateTime(duration / 1000));
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.play:
                if(!mediaPlayer.isPlaying()){
                    startPlay();
                }
                break;
            case R.id.pause:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();//暂停播放
                }
                break;
            case R.id.replay:
                mediaPlayer.stop();//停止播放
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startPlay();
                break;
            default:
                break;
        }
    }

    public void startPlay(){
        mediaPlayer.start();//开始播放
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
//        Log.i("TAG", "onStop: ");
    }
}