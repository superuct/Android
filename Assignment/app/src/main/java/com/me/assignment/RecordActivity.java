package com.me.assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {

    private RecordView recordView;
    private Button flash, switch_camera, photo, record;
    private boolean isRecording = false, isTiming = false;

    private Handler mHander=new Handler();
    private int mCount = 0;

    private Runnable mCounter = new Runnable() {
        @Override
        public void run() {
            mCount++;
            mHander.postDelayed(this,1000);
            if(mCount == 12){
                recordView.stopRecord();
                mHander.removeCallbacks(mCounter);
                isRecording = false;
                isTiming = false;
                mCount = 0;
                record.setText("Start");
                Toast.makeText(RecordActivity.this,"录制完成！",Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        recordView = findViewById(R.id.record_view);
        getSupportActionBar().hide();

        flash = findViewById(R.id.flash);
        switch_camera = findViewById(R.id.switch_camera);
        photo = findViewById(R.id.photo);
        record = findViewById(R.id.video);

        flash.setOnClickListener(this);
        switch_camera.setOnClickListener(this);
        photo.setOnClickListener(this);
        record.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flash:
                recordView.switchFlashMode();
                break;
            case R.id.switch_camera:
                recordView.switchCamera();
                break;
            case R.id.photo:
//                recordView.startCapture();
//                Log.i("TAG", "onClick: "+recordView.getCaptureFilePath());
                if(isRecording){
                    Toast.makeText(RecordActivity.this,"已经在录制了！",Toast.LENGTH_SHORT).show();
                }else{
                    mCount = 0;
                    mHander.post(mCounter);
                    recordView.startRecord();
                    isRecording = true;
                    isTiming = true;
                    record.setText("Stop");
                }
                break;
            case R.id.video:
                if(isRecording){
                    if(isTiming){
                        mHander.removeCallbacks(mCounter);
                        isTiming = false;
                        mCount = 0;
                    }
                    recordView.stopRecord();
                    isRecording = false;
                    record.setText("Start");
//                    Log.i("TAG", "onClick: "+recordView.getRecordFilePath());
                }else{
                    recordView.startRecord();
                    isRecording = true;
                    record.setText("Stop");
                }
                break;
            default:
                break;
        }
    }
}