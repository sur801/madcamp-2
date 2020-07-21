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
    TextView textView;
    TextView textView2;
    TextView textView3;
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

        textView = (TextView)v.findViewById(R.id.review_revise);
        textView2 = (TextView)v.findViewById(R.id.review_revise);
        textView3 = (TextView)v.findViewById(R.id.review_revise);

        // send Get request to server
        System.out.println("selected image#" + position + " " +imageList.get(position).getTitle());
        System.out.println("selected image#" + position + " " +imageList.get(position).getReview());
        System.out.println("selected image#" + position + " " +imageList.get(position).getLikes());

        String imageUrl = "http://192.249.19.241:3880/api/load/" + imageList.get(position).getName();
        String review = imageList.get(position).getReview();
        int likes = imageList.get(position).getLikes();
        String rates = imageList.get(position).getRate();

        Glide.with(mCont).load(imageUrl).into(imageView);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 1200);

        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setRotation(90);

        textView.setText(review);
        textView2.setText(Integer.toString(likes));
        textView3.setText(rates);

        container.addView(v);

        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();
    }



}
