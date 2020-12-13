package com.me.assignment;

public class Item {

    private String title;
    private String imageUrl;
    private String videoUrl;

    public Item(String title, String imageUrl, String videoUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getImageUrl() {
        return this.imageUrl;
    };
    public void setImageUrl(String url) {
        this.imageUrl = url;
    }
    public String getVideoUrl() {
        return this.videoUrl;
    };
    public void setVideoUrl(String url) {
        this.videoUrl = url;
    }
}
