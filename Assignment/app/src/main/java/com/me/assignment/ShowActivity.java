package com.me.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ShowActivity extends AppCompatActivity {

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api-sjtu-camp.bytedance.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    final UploadService myUpload = retrofit.create(UploadService.class);

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private String VideoUrl;

    private String source;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        getSupportActionBar().hide();

        surfaceView = findViewById(R.id.sv);

        Intent intent = getIntent();
        VideoUrl = intent.getStringExtra("path");
        source = intent.getStringExtra("code");

        initMediaPlayer();//初始化MediaPlayer

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mediaPlayer.isPlaying()){
                    startPlay();
                }else{
                    mediaPlayer.pause();//暂停播放
                }
            }
        });

        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myUpload.post(getMultipartFromAsset(VideoUrl), getMultipartFromAsset(VideoUrl),"1209","Eris").enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Toast.makeText(ShowActivity.this,"上传成功！",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
                Toast.makeText(ShowActivity.this,"已保存！",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        Button discard = findViewById(R.id.discard);
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(source.equals("record")){
                    deleteFile(VideoUrl);
                    MediaScannerConnection.scanFile(ShowActivity.this, new String[]{VideoUrl}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(uri);
                            sendBroadcast(mediaScanIntent);
                        }
                    });
                }
                Toast.makeText(ShowActivity.this,"已丢弃",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                startPlay();
//            }
//        });
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

//    private  byte[] fileNameToByte(String name){
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        try {
//            final InputStream inputStream = assetManager.open(name);
//
//            byte[] buffer = new byte[4096];
//            int n=0;
//            while(-1!=(n = inputStream.read(buffer))){
//                output.write(buffer, 0, n);
//            }
//
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//        return output.toByteArray();
//    }

    private MultipartBody.Part getMultipartFromAsset(String videoUrl){
        final String partKey = "video";

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
//            final InputStream inputStream = assetManager.open(videoUrl);
            final FileInputStream inputStream = new FileInputStream(new File(videoUrl));
            byte[] buffer = new byte[4096];
            int n=0;
            while(-1!=(n = inputStream.read(buffer))){
                output.write(buffer, 0, n);
            }

        } catch (IOException e){
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form_data"), output.toByteArray());
        return MultipartBody.Part.createFormData(partKey, videoUrl, requestBody);
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
                    mediaPlayer.setLooping(true);
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
    }
    //使用时弹出提示框
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}