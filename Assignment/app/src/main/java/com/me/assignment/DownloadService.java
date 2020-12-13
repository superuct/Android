package com.me.assignment;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DownloadService {
    @GET("invoke/video")
    Call<WebBody> getWebBody();
}
