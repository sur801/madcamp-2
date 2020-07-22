package com.example.myapplication;
import com.example.myapplication.ui.login.LoginActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    Button logoutButton;
    private LoginButton facebook_logout;
    ApiService apiService;
    List<ImageInfo> imageList = new ArrayList<>();

    static final int REQUEST = 0;
    TextView emailText;
    TextView imageCount;
    AppBarConfiguration mAppBarConfiguration;

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
    public void getImageList(String email) {

        Call<List<ImageInfo>> req2 = apiService.getList(email);
        req2.enqueue(new Callback<List<ImageInfo>>() {
            @Override
            public void onResponse(Call<List<ImageInfo>> call, Response<List<ImageInfo>> response) {
                imageList = response.body();
                int image_cnt = imageList.size();
                imageCount.setText("내가 올린 리뷰 수 : " + image_cnt);

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
        setContentView(R.layout.activity_main);
        emailText = (TextView)findViewById(R.id.user_email);
        logoutButton = (Button)findViewById(R.id.db_logout);
        facebook_logout = (LoginButton) findViewById(R.id.user_logout);
        imageCount = (TextView)findViewById(R.id.user_images);

        System.out.println("main on create");
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_feed)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // 현재 이메일 임시로 저장해두기
        SharedPreferences sharedPreferences = this.getSharedPreferences("loginFile",Context.MODE_PRIVATE);
        String cur_email = sharedPreferences.getString("tmp_email", null);
        Log.d("afdasfasf", "Main onCreate" + cur_email);

        initRetrofitClient();
        getImageList(cur_email);
        emailText.setText(cur_email);

        // 연락처 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,}, REQUEST);
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        facebook_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccessToken accessToken=AccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                if(!isLoggedIn) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }

            }
        });


    }
}