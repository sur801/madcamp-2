package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    List<ImageInfo> item_list = null;
    private Context mContext;
    GridImageFragment homeFragment;
    ApiService apiService;

    // 텔레비전같은 것이라고 이해하자
    public class ViewHolder extends RecyclerView.ViewHolder {
        // 일종의 채널. 어떤 신호가 오느냐에따라 다른 채널을 틀 수 있다.
        ImageView icon;

        // 그렇다면, 어떻게 신호를 채널과 연결할 수 있는가? 이걸 바로 이 생성자를 통해서 한다.
        ViewHolder(View gallery_item) {  // gallery_item 이라는 신호를 받으면
            super(gallery_item);        // 상위 뷰홀더에 일단 신호를 주고
            icon = gallery_item.findViewById(R.id.item_gallery);    // 이 신호를 텔레비전의 채널에 연결시켜준다
        }
    }

    // 텔레비전을 어떤 벽에 달아서 통째로 준다고 생각하자. 즉, 뷰홀더를 어떤 뷰 그룹에 달아서 반환할 것인가?
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_gallery_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    private void initRetrofitClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS).build();

        apiService = new Retrofit.Builder().baseUrl("http://192.249.19.241:3880/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client).build().create(ApiService.class);
    }

    public void syncImageList(int pos) {

        //get my current email
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("loginFile", Context.MODE_PRIVATE);
        String cur_email = sharedPreferences.getString("tmp_email", null);
        Call<List<ImageInfo>> req2 = apiService.getList(cur_email);
        System.out.println("getSync Start");
        req2.enqueue(new Callback<List<ImageInfo>>() {

            @Override
            public void onResponse(Call<List<ImageInfo>> call, Response<List<ImageInfo>> response) {
                System.out.println("getSync MID1");

                // image list
                List<ImageInfo> tempImageList = response.body();

                //server image lsit size
                int tempSize = tempImageList.size();
                int originSize = item_list.size();

                if(tempSize != originSize) {
                    item_list.remove(pos);
                }
            }

            @Override
            public void onFailure(Call<List<ImageInfo>> call, Throwable t) {
                System.out.println("getImageList MID2");
                System.out.println(call.toString());
                System.out.println(t.toString());
            }
        });
        homeFragment.refreshFragment();
        System.out.println("getSync End");
    }

    // delete selected image from server
    public void deleteItem(int pos) {
        Call<ResponseBody> del = apiService.deleteImage(item_list.get(pos).getName());
        del.enqueue(new Callback<ResponseBody>() {
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

    // 일종의 리모콘이다. position이라는 신호 번호가 왔으면, 이에 대응되는 텔레비전의 채널을 틀어준다
    @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // get name of selected image
            String imageName = item_list.get(position).getName();

        // send Get request to server
        String imageUrl = "http://192.249.19.241:3880/api/load/" + imageName;



        Glide.with(mContext).load(imageUrl).into(holder.icon);

        holder.icon.setLayoutParams(new RelativeLayout.LayoutParams(370, 370));
        holder.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.icon.setPadding(1, 1, 1, 1);

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("이미지 삭제").setMessage("삭제하시겠습니까?");

        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                initRetrofitClient();
                // delete image at server
                deleteItem(position);

                //sync with local and server
                syncImageList(position);

            }
        });

            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // delete cancle
                }
            });

        AlertDialog alertDialog = builder.create();

        holder.icon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                alertDialog.show();
                return true;
            }
        });

        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ReviewActivity.class);
                intent.putExtra("name",item_list.get(position).getName());
                intent.putExtra("pos", position);
                mContext.startActivity(intent);
            }
        });

    }

    // 총 몇 개의 신호가 들어왔는지 반환해준다
    @Override
    public int getItemCount() {
        return item_list.size();
    }

    GalleryAdapter(List<ImageInfo> list, Context mContext, GridImageFragment homeFragment ) {
        item_list = list;
        this.mContext = mContext;
        this.homeFragment = homeFragment;
    }

}
