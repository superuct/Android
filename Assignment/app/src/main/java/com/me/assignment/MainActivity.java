package com.me.assignment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api-sjtu-camp.bytedance.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    List<Item> newsList = new ArrayList<>();
    ItemViewAdapter newsAdapter = new ItemViewAdapter();
    private SwipeRefreshLayout srl;

    private int REQUEST_SDCARD = 1;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RecordActivity.class));
            }
        });

        RecyclerView view = findViewById(R.id.recycle_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        view.setLayoutManager(layoutManager);
        view.setAdapter(newsAdapter);

        refreshList();

        SearchLayout searchLayout = findViewById(R.id.search);
        searchLayout.setOnSearchTextChangedListener(new SearchLayout.OnSearchTextChangedListener() {
            @Override
            public void afterChanged(String text) {
                List<Item> filters = new ArrayList<>();
                for(Item i:newsList){
                    if(i.getTitle().contains(text)){
                        filters.add(i);
                    }
                }
                newsAdapter.updateItemView(filters);
            }
        });

        srl = (SwipeRefreshLayout) findViewById(R.id.swipe);
        srl.setColorSchemeColors(R.color.design_default_color_primary);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void refresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);//为了体现出刷新效果，所以这里休眠了线程
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //切回主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshList();
                        srl.setRefreshing(false);//当刷新事件结束时，隐藏刷新进度条
                    }
                });
            }
        }).start();
    }

    public void refreshList(){

        DownloadService downloadService = retrofit.create(DownloadService.class);
        Call<WebBody> call = downloadService.getWebBody();
        call.enqueue(new Callback<WebBody>() {
            @Override
            public void onResponse(Call<WebBody> call, Response<WebBody> response) {
                if (!response.isSuccessful()) {
                    return;
                }
//                Log.i("TAG", "onResponse: ");
                final WebBody webBody = response.body();
                final List<WebVideo> repoList = webBody.getFeeds();
                if (repoList == null || repoList.isEmpty()) {
                    return;
                }
//                final List<String> items = new ArrayList<>();
                newsList.clear();
                for (int i = 0; i < repoList.size(); i++) {
                    final WebVideo repo = repoList.get(i);
                    newsList.add(new Item(repo.getUser_name(), repo.getImage_url(), repo.getVideo_url()));
//                    Log.i("TAG", "onResponse: "+repo.getUser_name()+repo.getImage_url());
                }
                newsAdapter.updateItemView(newsList);
            }

            @Override
            public void onFailure(Call<WebBody> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SDCARD && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
//                Log.i("TAG", "onActivityResult: "+path);

                final Bundle bundle =new Bundle();
                bundle.putString("code", "main");
                bundle.putString("path", path);

                Intent intent = new Intent(this, SelectCoverActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_SDCARD);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}