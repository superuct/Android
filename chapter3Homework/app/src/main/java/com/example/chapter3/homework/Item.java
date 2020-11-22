package com.example.chapter3.homework;

import java.io.Serializable;
import java.util.ArrayList;

public class Item implements Serializable {
    private static final long serialVersionUID = -6099312954099962806L;
    private String title;
    private String body;

    public Item(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public static ArrayList<Item> getItems() {
        ArrayList<Item> items = new ArrayList<Item>();
        for(int i=0;i<100;i++){
            items.add(new com.example.chapter3.homework.Item("好友 "+String.valueOf(i), "你好啊， 我是小 "+String.valueOf(i)));
        }
        return items;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
