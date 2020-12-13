package com.me.assignment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ItemViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private List<Item> newsList = new ArrayList<>();
    View view;

//    public ItemViewAdapter(List<Item> newsList) {
//        this.newsList = newsList;
//    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item,viewGroup,false);
        ItemViewHolder viewHolder = new ItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int i) {
        viewHolder.bind(newsList.get(i));
//        Item news = newsList.get(i);
//        viewHolder.newsImage.setImageResource(news.getPic());
//        Glide.with(view.getContext())
//                .load(news.getImageUrl())
//                .error(R.drawable.error)
//                .into(viewHolder.coverImage);
//
////        ViewGroup.LayoutParams params = viewHolder.newsImage.getLayoutParams();
////        params.height = params.height + new Random().nextInt(300);
////        viewHolder.newsImage.setLayoutParams(params);
//
//        viewHolder.userName.setText(news.getTitle());
//        viewHolder.videoUrl = news.getVideoUrl();
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void updateItemView(@NonNull List<Item> items){
        newsList.clear();
        newsList.addAll(items);
        notifyDataSetChanged();
    }
}