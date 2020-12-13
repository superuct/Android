package com.bytedance.network.api;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface MyUpload {
//    @POST("/post")
//    Call<ResponseBody> post(@Query("address") String address);
    @Multipart
    @POST("/invoke/video")
    Call<ResponseBody> post(@Part MultipartBody.Part image,
                            @Query("student_id") String student_id,
                            @Query("user_name") String user_name);
}
