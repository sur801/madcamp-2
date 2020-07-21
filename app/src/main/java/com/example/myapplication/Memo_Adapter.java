package com.example.myapplication;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/*
* 1. 뷰 홀더를 정의해준다 (클래스 정의) -> 여기에 어떤 형식의 아이템뷰를 보여줄 것인지 설정
* 2. 뷰 홀더를 호출해준다 (생성자 함수 호출)
* 3.
* */

public class Memo_Adapter extends RecyclerView.Adapter<Memo_Adapter.ViewHolder> {
    public List<ImageInfo> memo_data;

    Context mContext;


    // 데이터를 전달받아온다.
    public Memo_Adapter(List<ImageInfo> list, Context context){
        this.memo_data = list;
        System.out.println("image info size + " +this.memo_data.size());

        this.mContext = context;
    }

    // 뷰 홀더가 어떤 아이템 뷰를 보여줄 것인지 설정한다 (일종의 텔레비젼)
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView email;
        TextView review;
        ImageView icon;


        // 전달받은 신호를 아이템 뷰의 어떤 부분에 표시할 것인지 설정한다
        ViewHolder(View view){
            super(view);

            title = view.findViewById(R.id.title);
            email = view.findViewById(R.id.writer_email);
            icon = view.findViewById(R.id.imageView2);
            review = view.findViewById(R.id.review);
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
        holder.title.setText("영화제목");
        holder.email.setText(here.getEmail());
        holder.review.setText(here.getReview());

        Glide.with(mContext).load(imageUrl).into(holder.icon);

    }

    // 갖고 있는 아이템 데이터 갯수를 반환한다. 나중에 사용됨.
    @Override
    public int getItemCount() {
        return memo_data.size();
    }
}
