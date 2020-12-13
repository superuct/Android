package com.me.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    ImageView coverImage;
    TextView userName;
    String videoUrl;
    View view;


    public ItemViewHolder(@NonNull View itemView) {
        super(itemView);
        coverImage = itemView.findViewById(R.id.newsPic);
        userName = itemView.findViewById(R.id.newsTitle);
        itemView.setOnClickListener(this);
        view = itemView;
    }

    public void bind(Item news){

        Glide.with(view.getContext())
                .load(news.getImageUrl())
                .error(R.drawable.error)
                .into(coverImage);

        userName.setText(news.getTitle());
        videoUrl = news.getVideoUrl();
    }

    @Override
    public void onClick(View v) {
//        Log.i("TAG", "onClick: to video"+userName.getText().toString()+videoUrl);
        final Bundle bundle =new Bundle();
        bundle.putString("user_name",userName.getText().toString());
        bundle.putString("video_url",videoUrl);

        Intent intent = new Intent(v.getContext(), VideoActivity.class);
        intent.putExtras(bundle);
        v.getContext().startActivity(intent);

//        Toast.makeText(v.getContext(),"click", Toast.LENGTH_SHORT).show();
    }
}

