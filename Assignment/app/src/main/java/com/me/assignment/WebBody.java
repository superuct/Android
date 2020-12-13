package com.me.assignment;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WebBody {
    @SerializedName("feeds")
    private List<WebVideo> feeds;

    @SerializedName("success")
    private boolean success;

    public List<WebVideo> getFeeds(){return feeds;};
    public boolean getSuccess(){return success;};
}
