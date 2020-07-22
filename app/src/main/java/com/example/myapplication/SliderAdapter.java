package com.example.myapplication;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SliderAdapter extends PagerAdapter {

    private LayoutInflater inflater;
    private Context mCont;
    ImageButton delButton;
    ImageView imageView;
    TextView textView_title;
    TextView textView_rates;
    TextView textView_content;
    TextView textView_likes;
    List<ImageInfo> imageList = new ArrayList<>();

    public SliderAdapter(Context context) {
        System.out.println("생성");
        mCont = context;
    }

    public void setItem( List<ImageInfo> item){
        this.imageList = item;

    }

    @Override
    public int getCount() {
        return imageList.size();
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ConstraintLayout) object);

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        inflater = (LayoutInflater)mCont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.slider, container, false);

        imageView = (ImageView)v.findViewById(R.id.imageView);
        System.out.println("hello");

        textView_title = (TextView)v.findViewById(R.id.review_title);
        textView_rates = (TextView)v.findViewById(R.id.review_rates);
        textView_content = (TextView)v.findViewById(R.id.review_content);
        textView_likes = (TextView)v.findViewById(R.id.review_likes);

        // send Get request to server
        System.out.println("selected image#" + position + " " +imageList.get(position).getTitle());
        System.out.println("selected image#" + position + " " +imageList.get(position).getReview());
        System.out.println("selected image#" + position + " " +imageList.get(position).getRate());

        String imageUrl = "http://192.249.19.241:3880/api/load/" + imageList.get(position).getName();
        String review = imageList.get(position).getReview();
        String rate = imageList.get(position).getRate();
        String title = imageList.get(position).getTitle();
        int likes = imageList.get(position).getLikes();

        Glide.with(mCont).load(imageUrl).into(imageView);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500);

        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        textView_title.setText(title);
        textView_rates.setText(rate);
        textView_content.setText(review);
        textView_likes.setText(Integer.toString(likes));

        container.addView(v);

        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();
    }
}
