package com.me.chapter8;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class PictureActivity extends AppCompatActivity {
    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");

        ZoomImageView imageView = findViewById(R.id.zoom_image);
        Glide.with(this)
                .load(path)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .error(R.drawable.error)
                .fitCenter()
                //.transition(withCrossFade(4000))
                //.override(100, 100)
                .into(imageView);

        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PictureActivity.this,"已保存",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        Button discard = findViewById(R.id.discard);
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = getContentResolver();
                String where = MediaStore.Images.Media.DATA + "='" + path + "'";
//删除图片
                mContentResolver.delete(uri, where, null);
                MediaScannerConnection.scanFile(PictureActivity.this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(uri);
                        sendBroadcast(mediaScanIntent);
                    }
                });
                Toast.makeText(PictureActivity.this,"已丢弃",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
