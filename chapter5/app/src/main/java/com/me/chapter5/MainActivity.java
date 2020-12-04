package com.me.chapter5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.DialogTitle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private GithubAdapter mSearchAdapter = new GithubAdapter();
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btn);
        TextView tv = findViewById(R.id.tv);

        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mSearchAdapter);



        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GithubService service = retrofit.create(GithubService.class);
                Call<List<Repo>> call = service.getRepos("JakeWharton");
                call.enqueue(new Callback<List<Repo>>() {
                    @Override public void onResponse(final Call<List<Repo>> call, final Response<List<Repo>> response) {
                        // 合法性校验
                        if (!response.isSuccessful()) {
                            return;
                        }
                        final List<Repo> repoList = response.body();
                        if (repoList == null || repoList.isEmpty()) {
                            return;
                        }
                        Log.i("TAG", "onResponse: ");
                        final List<String> items = new ArrayList<>();
//                        for(int i=0;i<100;i++){
//                            items.add("这是第"+i+"行！");
//                        }
//                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < repoList.size(); i++) {
                            final Repo repo = repoList.get(i);
                            //stringBuilder.append("仓库名：" + repo.getName() + "\n");
                            items.add("仓库名：" + repo.getName());
                        }
//                        tv.setText(stringBuilder.toString());
                        mSearchAdapter.notifyItems(items);
                    }

                    @Override public void onFailure(final Call<List<Repo>> call, final Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        });
    }
}