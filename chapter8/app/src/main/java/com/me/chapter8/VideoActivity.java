package com.me.chapter8;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
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

    private String path;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");

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

        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(VideoActivity.this,"已保存",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        Button discard = findViewById(R.id.discard);
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                ContentResolver mContentResolver = getContentResolver();
//                String where = MediaStore.Images.Media.DATA + "='" + path + "'";
//                mContentResolver.delete(uri, where, null);

                deleteFile(path);

                MediaScannerConnection.scanFile(VideoActivity.this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(uri);
                        sendBroadcast(mediaScanIntent);
                    }
                });
                Toast.makeText(VideoActivity.this,"已丢弃",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    public boolean deleteFile(String path) {
        File file = null;
        if (path != null && path.length() > 0) {
            file = new File(path);
        }
        if (file.exists()) { // 判断文件是否存在
            file.delete();
            return true;
        }else {
            return false;
        }
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
            mediaPlayer.setDataSource(path);
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