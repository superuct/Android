package com.me.assignment;

import com.google.gson.annotations.SerializedName;

public class WebVideo {
    @SerializedName("_id")
    private String _id;

    @SerializedName("student_id")
    private String student_id;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("video_url")
    private String video_url;

    @SerializedName("image_url")
    private String image_url;

    @SerializedName("image_w")
    private int image_w;

    @SerializedName("image_h")
    private int image_h;

    public String get_id(){return _id;};
    public String getStudent_id(){return student_id;};
    public String getUser_name(){return user_name;};
    public String getVideo_url(){return video_url;};
    public String getImage_url(){return image_url;};
    public int getImage_w(){return image_w;};
    public int getImage_h(){return image_h;};

}