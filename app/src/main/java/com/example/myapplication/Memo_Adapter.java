package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
* 1. 뷰 홀더를 정의해준다 (클래스 정의) -> 여기에 어떤 형식의 아이템뷰를 보여줄 것인지 설정
* 2. 뷰 홀더를 호출해준다 (생성자 함수 호출)
* 3.
* */

public class Memo_Adapter extends RecyclerView.Adapter<Memo_Adapter.ViewHolder> {
    public List<ImageInfo> memo_data = new ArrayList<>();
    public List<Item> movie_data = new ArrayList<>();

    Context mContext;
    ApiService apiService;

    // 데이터를 전달받아온다.
    public Memo_Adapter(Context context){
        System.out.println("image info size + " +this.memo_data.size());

        this.mContext = context;
    }
    public void setItem(List<ImageInfo> imlist){
        memo_data = imlist;

    };

    private void initRetrofitClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS).build();

        apiService = new Retrofit.Builder().baseUrl("http://192.249.19.241:3880/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client).build().create(ApiService.class);
    }

    private void initMovie() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS).build();

        apiService = new Retrofit.Builder().baseUrl("https://openapi.naver.com/v1/search/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client).build().create(ApiService.class);
    }

    public void loadMovie(String query, String display) {
        Call<MovieInfo> movie = apiService.loadMovie(query, display, 1);
        movie.enqueue(new Callback<MovieInfo>() {
            @Override
            public void onResponse(Call<MovieInfo> call, Response<MovieInfo> response) {
                System.out.println(" MID1");
                movie_data = new ArrayList(response.body().getItems());
                int start = response.body().getStart();

                Item item = movie_data.get(start-1);
                System.out.println(movie_data.size());
                //alert dialg로 영화 정보 띄우기
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(item.getTitle());
                String content ;
                content = "개봉 년도 : " + item.getPubDate() + "\n" + "감독 : " + item.getDirector() + "\n" + "출연 배우들 : " + item.getActor() + "\n" + "네이버 평점 : " + item.getUserRating();
                builder.setMessage(content);
                builder.setPositiveButton("확인", null);
                builder.create().show();
            }

            @Override
            public void onFailure(Call<MovieInfo> call, Throwable t) {
                System.out.println("deleteItem MID2");
                System.out.println(call.toString());
                System.out.println(t.toString());
            }
        });
    }

    // delete selected image from server
    public void updateItem(String name) {
        Call<ResponseBody> update = apiService.updateImage(name);
        update.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println(" MID1");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("deleteItem MID2");
                System.out.println(call.toString());
                System.out.println(t.toString());
            }
        });
    }

    // 뷰 홀더가 어떤 아이템 뷰를 보여줄 것인지 설정한다 (일종의 텔레비젼)
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView email;
        ImageView thumbnail;
        TextView rate;
        TextView review;
        TextView likes;
        ImageView heart;
        ImageView star;
        ImageView info;


        // 전달받은 신호를 아이템 뷰의 어떤 부분에 표시할 것인지 설정한다
        ViewHolder(View view){
            super(view);

            title = view.findViewById(R.id.title);
            email = view.findViewById(R.id.writer_email);
            thumbnail = view.findViewById(R.id.imageView2);
            rate = review = view.findViewById(R.id.rate);
            review = view.findViewById(R.id.review);
            likes = view.findViewById(R.id.likes);
            star = view.findViewById(R.id.star);
            heart = view.findViewById(R.id.heart);
            info = view.findViewById(R.id.info);
        }
    }

    // 뷰 홀더를 호출해준다.
    // 이때, 어떤 뷰 그룹 위에 띄울 것인지 결정한다.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 표현하고자 하는 뷰 그룹의 배경
        Context context = parent.getContext();

        // 그냥 inflate용 단순 함수. 이때, 위에서 구한 context를 기반으로 inflater를 가져와야 한다
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 뷰 그룹 상에 어울리는 뷰 객체를 반환한다
        View view = inflater.inflate(R.layout.fragment_memo_date, parent, false);

        // 뷰 홀더 객체를 만들어준다 - 정의만 되어 있던 것을 실제고 객체화 해줌
        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    // position에 해당되는 데이터를 뷰홀더의 아이템 뷰에 표시한다
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 일단, 아이템 데이터를 받아온다. 그래야 뷰 홀더 이용해서 올릴 수 있지

        ImageInfo here = memo_data.get(position);

        String imageUrl = "http://192.249.19.241:3880/api/load/" + here.getName();
        holder.title.setText(here.getTitle());
        holder.email.setText(here.getEmail());
        System.out.println("email#" + position + " " + here.getEmail());
        holder.rate.setText(here.getRate());
        holder.review.setText(here.getReview());
        holder.likes.setText(Integer.toString(here.getLikes()));
        Glide.with(mContext).load(imageUrl).into(holder.thumbnail);
        holder.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.thumbnail.setLayoutParams(new LinearLayout.LayoutParams(450,450));
        String name = here.getName();
        boolean[] click = {false};
        holder.heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!click[0]) {
                    click[0] = true;
                    int like = here.getLikes();
                    like = like+1;
                    System.out.println("like : "+like);
                    holder.likes.setText(Integer.toString(like));
                    initRetrofitClient();
                    updateItem(name);
                }
            }
        });

        holder.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMovie();
                String movie = here.getTitle();
                loadMovie(movie, "20");

            }
        });

    }

    // 갖고 있는 아이템 데이터 갯수를 반환한다. 나중에 사용됨.
    @Override
    public int getItemCount() {
        return memo_data.size();
    }
}
