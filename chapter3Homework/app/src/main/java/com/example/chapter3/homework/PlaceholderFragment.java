package com.example.chapter3.homework;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class PlaceholderFragment extends Fragment {
    private TextView textView;
    private ListView listView;
    private int mAnimationTime = 300;
    private ArrayAdapter<Item> adapterItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create arraylist from item fixtures
        ArrayList<Item> items = Item.getItems();
        adapterItems = new ArrayAdapter<Item>(getActivity(),
                android.R.layout.simple_list_item_activated_1, items);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO ex3-3: 修改 fragment_placeholder，添加 loading 控件和列表视图控件
        View view = inflater.inflate(R.layout.fragment_placeholder, container, false);
        textView = view.findViewById(R.id.loading);
        listView = view.findViewById(R.id.lv);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 这里会在 5s 后执行
                // TODO ex3-4：实现动画，将 lottie 控件淡出，列表数据淡入
                // 设置内容contentView为0%的不透明度，但是状态为“可见”，
                // 因此在动画过程中是一直可见的（但是为全透明）。
                listView.setAlpha(0f);
                listView.setVisibility(View.VISIBLE);

                // 开始动画内容contentView到100%的不透明度，然后清除所有设置在View上的动画监听器。
                listView.animate().alpha(1f).setDuration(mAnimationTime)
                        .setListener(null);

                listView.setAdapter(adapterItems);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View item, int position,
                                            long rowId) {
                        // Retrieve item based on position
                        Item i = adapterItems.getItem(position);
                        // Fire selected event for item
//                listener.onItemSelected(i);
                    }
                });


                // 加载progressView开始动画逐渐变为0%的不透明度，
                // 动画结束后，设置可见性为GONE（消失）作为一个优化步骤
                // （它将不再参与布局的传递等过程）
                textView.animate().alpha(0f).setDuration(mAnimationTime)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                textView.setVisibility(View.GONE);
                            }
                        });

            }
        }, 5000);
    }
}
