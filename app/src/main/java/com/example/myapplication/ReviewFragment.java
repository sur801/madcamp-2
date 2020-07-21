package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ReviewFragment extends Fragment {
    public ReviewFragment() {
        // Required empty public constructor
    }

    // 리사이클러뷰 부분
    RecyclerView recyclerView;
    ReviewFragment_adapter memo_adapter;
    ArrayList<ReviewFragment_data_class> item_list;

    // 버튼 두 개 - 로그인, 추가
    FloatingActionButton _fab_memo;
    FloatingActionButton _fab_lock;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        // 데이터베이스에 있는 모든 내용들을 아이템에 넣어준다
        database_to_itemList();

        /* 리사이클러 뷰 관련 */
        // 리사이클러 뷰를 만든다
        recyclerView = view.findViewById(R.id.recycler_memo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        // 리사이클러 뷰에 대해 아답터를 설정해준다.
        recyclerView.setAdapter(memo_adapter);

        return view;
    }

    // 아이템을 아이템 리스트에 추가해준다 (리사이클러 뷰를 위해)
    public void addItem(String title, String date){
        ReviewFragment_data_class item = new ReviewFragment_data_class();

        item.setTitle(title);
        item.setDate(date);

        item_list.add(item);
    }

    // 데이터베이스에 있는 모든 내용들을 아이템에 넣어준다
    public void database_to_itemList(){
        item_list = new ArrayList<>();

        for (int i=0;i<10;i++){
            String title = "이거 되나";
            String date = "이것도??";

            addItem(title, date);
        }
    }
}