package com.me.chapter5;

import java.util.List;
import com.me.chapter5.Repo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GithubService {
    @GET("users/{username}/repos")
    Call<List<Repo>> getRepos(@Path("username") String userName);
}
