package com.me.chapter8;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecordView recordView;
    private Button flash, switch_camera, photo, record;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordView = findViewById(R.id.record_view);

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
                recordView.startCapture();
                Log.i("TAG", "onClick: "+recordView.getCaptureFilePath());
                break;
            case R.id.video:
                if(isRecording){
                    recordView.stopRecord();
                    isRecording = false;
                    Log.i("TAG", "onClick: "+recordView.getRecordFilePath());
                }else{
                    recordView.startRecord();
                    isRecording = true;
                }
                break;
            default:
                break;
        }
    }
}