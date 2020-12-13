package com.me.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SelectCoverActivity extends AppCompatActivity implements View.OnClickListener {
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api-sjtu-camp.bytedance.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    final UploadService myUpload = retrofit.create(UploadService.class);

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    protected TextView tv_start;
    protected TextView tv_end;
    protected SeekBar seekBar;
    private Timer timer;//定时器
    private boolean isSeekbarChaning;//互斥变量，防止进度条和定时器冲突。

    private Button discard, toAlbum, takePhoto, saveUpload;

    private String VideoUrl, code;
    private int REQUEST_SDCARD = 1;
    private int REQUEST_CAPTURE = 2;
    private String imgPath;
    private String time;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_cover);
        getSupportActionBar().hide();

        surfaceView = findViewById(R.id.sv);
        tv_start = (TextView)findViewById(R.id.curr_time);
        tv_end = (TextView)findViewById(R.id.duration);
        seekBar = (SeekBar)findViewById(R.id.seekbar);

        discard = findViewById(R.id.discard);
        toAlbum = findViewById(R.id.album);
        takePhoto = findViewById(R.id.take_photo);
        saveUpload = findViewById(R.id.save);

        Intent intent = getIntent();
        code = intent.getStringExtra("code");
        VideoUrl = intent.getStringExtra("path");

        initView();//初始化进度条
        initMediaPlayer();//初始化MediaPlayer


        surfaceView.setOnClickListener(this);
        discard.setOnClickListener(this);
        toAlbum.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        saveUpload.setOnClickListener(this);

//        user_name.setText(UserName);

//        surfaceView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!mediaPlayer.isPlaying()){
//                    startPlay();
//                }else{
//                    mediaPlayer.pause();//暂停播放
//                }
//            }
//        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startPlay();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.i("TAG", "onActivityResult: "+String.valueOf(requestCode));
        if (requestCode == REQUEST_SDCARD && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
//                Log.i("TAG", "onActivityResult: "+path);
                MyPost(path,VideoUrl,"1210","Eris");
                finish();
            }
        }
        if (requestCode == REQUEST_CAPTURE && resultCode == RESULT_OK) {
            MyPost(imgPath, VideoUrl,"1210","Eris");
//            Log.i("TAG", "onActivityResult: "+imgPath);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sv:
                if(!mediaPlayer.isPlaying()){
                    startPlay();
                }else{
                    mediaPlayer.pause();//暂停播放
                }
                break;
            case R.id.discard:
                if(code.equals("record")){
                    deleteFile(VideoUrl);
                    MediaScannerConnection.scanFile(SelectCoverActivity.this, new String[]{VideoUrl}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(uri);
                            sendBroadcast(mediaScanIntent);
                        }
                    });
                }
                Toast.makeText(SelectCoverActivity.this,"已丢弃",Toast.LENGTH_SHORT).show();
                finish();
                break;
            case R.id.album:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_SDCARD);
//                Log.i("TAG", "onClick: "+recordView.getCaptureFilePath());
                break;
            case R.id.take_photo:
                Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                imgPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM+File.separator+"Camera"+File.separator+"IMG_"+time+".jpg";
                File mCaptureFile = new File(imgPath);
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, mCaptureFile.getAbsolutePath());
                Uri fileUri = getApplication().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent1, REQUEST_CAPTURE);
                break;
            case R.id.save:
                time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                imgPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM+File.separator+"Camera"+File.separator+"IMG_"+time+".jpg";
                try {
                    boolean flag = getSurfaceViewScreenshot(VideoUrl, imgPath, mediaPlayer, v.getContext());
                    if(flag){
                        MyPost(imgPath, VideoUrl,"1210","Eris");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
//                MyPost();
                finish();
                break;
            default:
                break;
        }
    }

    public void MyPost(String imgUrl, String vidUrl, String id, String user_name){
        myUpload.post(getMultipartFromAsset("cover_image",imgUrl), getMultipartFromAsset("video",vidUrl),id,user_name).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(SelectCoverActivity.this,"上传成功！",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private MultipartBody.Part getMultipartFromAsset(String partKey, String url){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
//            final InputStream inputStream = assetManager.open(videoUrl);
            final FileInputStream inputStream = new FileInputStream(new File(url));
            byte[] buffer = new byte[4096];
            int n=0;
            while(-1!=(n = inputStream.read(buffer))){
                output.write(buffer, 0, n);
            }

        } catch (IOException e){
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form_data"), output.toByteArray());
        return MultipartBody.Part.createFormData(partKey, url, requestBody);
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

    /**
     * @Title: getVideoFrame
     * @Description: 获取视频某帧的图像，但得到的图像并不一定是指定position的图像。
     * @param path     视频的本地路径
//     * @param position 视频流播放的position
     * @return Bitmap 返回的视频图像
     * @throws
     */
    @SuppressLint("NewApi")
    public static boolean getSurfaceViewScreenshot(String videoUrl, String imgpath, MediaPlayer mediaPlayer, Context context) throws FileNotFoundException {

        Bitmap bmp = null;
        // android 9及其以上版本可以使用该方法
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoUrl);
            // 这一句是必须的
            String timeString =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            // 获取总长度,这一句也是必须的
            long titalTime = Long.parseLong(timeString) * 1000;

            long videoPosition = 0;
//                mediaPlayer.setDataSource(path);
//                if (path.startsWith("http")) {
//                    mediaPlayer.prepareAsync();
//                } else {
//                    mediaPlayer.prepare();
//                }
            int duration = mediaPlayer.getDuration();
            int position = mediaPlayer.getCurrentPosition();
            // 通过这个计算出想截取的画面所在的时间
            videoPosition = titalTime * position / duration;
            if (videoPosition > 0) {
                bmp = retriever.getFrameAtTime(videoPosition,
                        MediaMetadataRetriever.OPTION_CLOSEST);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        File mFile = new File(imgpath);                        //将要保存的图片文件
        FileOutputStream outputStream = new FileOutputStream(mFile);     //构建输出流
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);  //compress到输出outputStream
        Uri uri = Uri.fromFile(mFile);                                  //获得图片的uri
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)); //发送广播通知更新图库，这样系统图库可以找到这张图片
        return true;
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