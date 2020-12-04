package com.me.chapter5;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TextViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView mTextView;

    public TextViewHolder(@NonNull View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.text);
        itemView.setOnClickListener(this);
    }

    public void bind(String text){
        mTextView.setText(text);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(v.getContext(), mTextView.getText().toString(),Toast.LENGTH_SHORT).show();

    }
}

