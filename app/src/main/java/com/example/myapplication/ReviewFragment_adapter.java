package com.example.myapplication;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*
 * 1. 뷰 홀더를 정의해준다 (클래스 정의) -> 여기에 어떤 형식의 아이템뷰를 보여줄 것인지 설정
 * 2. 뷰 홀더를 호출해준다 (생성자 함수 호출)
 * 3.
 * */

public class ReviewFragment_adapter extends RecyclerView.Adapter<ReviewFragment_adapter.ViewHolder> {
    private ArrayList<ReviewFragment_data_class> memo_data = null;

    // 데이터를 전달받아온다.
    public ReviewFragment_adapter(ArrayList<ReviewFragment_data_class> list){
        memo_data = list;
    }

    // 뷰 홀더가 어떤 아이템 뷰를 보여줄 것인지 설정한다 (일종의 텔레비젼)
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView date;
        TextView email;

        // 전달받은 신호를 아이템 뷰의 어떤 부분에 표시할 것인지 설정한다
        ViewHolder(View ReviewFragment_data_class){
            super(ReviewFragment_data_class);

            title = ReviewFragment_data_class.findViewById(R.id.memo_title);
            date = ReviewFragment_data_class.findViewById(R.id.memo_date);
            email = ReviewFragment_data_class.findViewById(R.id.memo_email);
        }
    }

    // 뷰 홀더를 호출해준다.
    // 이때, 어떤 뷰 그룹 위에 띄울 것인지 결정한다.
    @NonNull
    @Override
    public ReviewFragment_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 표현하고자 하는 뷰 그룹의 배경
        Context context = parent.getContext();

        // 그냥 inflate용 단순 함수. 이때, 위에서 구한 context를 기반으로 inflater를 가져와야 한다
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 뷰 그룹 상에 어울리는 뷰 객체를 반환한다
        View view = inflater.inflate(R.layout.fragment_review_item, parent, false);

        // 뷰 홀더 객체를 만들어준다 - 정의만 되어 있던 것을 실제고 객체화 해줌
        ReviewFragment_adapter.ViewHolder vh = new ReviewFragment_adapter.ViewHolder(view);

        return vh;
    }

    // position에 해당되는 데이터를 뷰홀더의 아이템 뷰에 표시한다
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 일단, 아이템 데이터를 받아온다. 그래야 뷰 홀더 이용해서 올릴 수 있지
        ReviewFragment_data_class item_data = memo_data.get(position);

        // 이제 객체화 되었기 때문에, 그대로 데이터만 전달해주면 된다.
        holder.title.setText(item_data.getTitle());
        holder.date.setText(item_data.getDate());
        holder.email.setText(item_data.getEmail());
    }

    // 갖고 있는 아이템 데이터 갯수를 반환한다. 나중에 사용됨.
    @Override
    public int getItemCount() {
        return memo_data.size();
    }
}
