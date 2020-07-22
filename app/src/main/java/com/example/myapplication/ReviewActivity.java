package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReviewActivity extends AppCompatActivity {
    private static final String TAG = "ReviewActivity";
    ViewPager mViewPager;
    SliderAdapter Adapter;
    ApiService apiService;
    SharedPreferences sharedPreferences;
    List<ImageInfo> imageList = new ArrayList<>();
    int pos;
    Context context;




    // init retrofit to use library
    private void initRetrofitClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS).build();

        apiService = new Retrofit.Builder().baseUrl("http://192.249.19.241:3880/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client).build().create(ApiService.class);
    }


    // get imagelist from db
    public void getImageList() {
        System.out.println("getImageList START");
        //get my current email
        sharedPreferences = getSharedPreferences("loginFile", Context.MODE_PRIVATE);
        String cur_email = sharedPreferences.getString("tmp_email", null);
        System.out.println("cur_email" + cur_email);
        Call<List<ImageInfo>> req2 = apiService.getList(cur_email);
        req2.enqueue(new Callback<List<ImageInfo>>() {
            @Override
            public void onResponse(Call<List<ImageInfo>> call, Response<List<ImageInfo>> response) {
                imageList = response.body();
                System.out.println("image size " + imageList.size());

                System.out.println("review activity : " + imageList.size());
                Adapter = new SliderAdapter(context);
                Adapter.setItem(imageList);
                mViewPager.setAdapter(Adapter);
                System.out.println("pos " + pos);
                Adapter.notifyDataSetChanged();
                mViewPager.setCurrentItem(pos);

            }

            @Override
            public void onFailure(Call<List<ImageInfo>> call, Throwable t) {
                System.out.println("getImageList MID2");
                System.out.println(call.toString());
                System.out.println(t.toString());
            }
        });

        System.out.println("getImageList END");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_layout);
        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        Intent intent = getIntent();
        String name = intent.getExtras().getString("name");
        pos = intent.getExtras().getInt("pos");
        context = this;
        initRetrofitClient();
        getImageList();


    }
}