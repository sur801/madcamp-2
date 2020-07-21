package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.ls.LSOutput;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

public class MemoFragment extends Fragment {
    public MemoFragment() {
        // Required empty public constructor
    }

    // 리사이클러뷰 부분
    RecyclerView recyclerView;
    Memo_Adapter memo_adapter;
    ArrayList<Memo_data_class> item_list;

    View view;
    // email
    String current_email;
    String one_friend_email;
    ArrayList<String> friend_email_list = new ArrayList<String>();
    List<ImageInfo> feed_list = new ArrayList<>();
    ApiService apiService;

    // 호출용 (all)
    JSONArray allArray;

    // refresh fragment when image data updated
    public void refreshFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.detach(this).attach(this).commit();
    }


    public void get_feed(String email){
        Call<List<ImageInfo>> req2 = apiService.getList(email);
        req2.enqueue(new Callback<List<ImageInfo>>() {
            @Override
            public void onResponse(Call<List<ImageInfo>> call, Response<List<ImageInfo>> response) {
                // temp image list
                List<ImageInfo> temp = null;
                // image list
                temp = response.body();
                for(int i=0 ; i<temp.size() ; i++){
                    feed_list.add(temp.get(i));
                }
                if(temp.size()!=0) {
                    memo_adapter.memo_data = feed_list;
                    memo_adapter.notifyDataSetChanged();
                }
            }


            @Override
            public void onFailure(Call<List<ImageInfo>> call, Throwable t) {
                System.out.println("getImageList MID2");
                System.out.println(call.toString());
                System.out.println(t.toString());
            }
        });

    }

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_memo, container, false);

        // 현재 내 이메일을 가져온다
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginFile",Context.MODE_PRIVATE);
        current_email = sharedPreferences.getString("tmp_email", null);
        System.out.println("current_email ! : " + current_email+"}");


        // 데이터베이스에 있는 모든 내용들을 아이템에 넣어준다
        initRetrofitClient();
        //syncFeed();

        new get_email().execute("http://192.249.19.241:3880/api/addressbook/phone_all");//AsyncTask 시작시킴
        System.out.println("get email ");

        /* 리사이클러 뷰 관련 */
        // 리사이클러 뷰를 만든다
        recyclerView = view.findViewById(R.id.recycler_memo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // 메모 아답터를 생성해준다
        System.out.println("make adapter");
        System.out.println("feed size : " + feed_list.size());
        memo_adapter = new Memo_Adapter(feed_list , getActivity());


        // 현재 내 이메일을 가져온다
        sharedPreferences = getActivity().getSharedPreferences("loginFile",Context.MODE_PRIVATE);
        current_email = sharedPreferences.getString("tmp_email", null);
        System.out.println("current_email ! : " + current_email+"}");
      
        // 데이터베이스에 있는 모든 내용들을 아이템에 넣어준다
        initRetrofitClient();
        //syncFeed();

        new get_email().execute("http://192.249.19.241:3880/api/addressbook/phone_all");//AsyncTask 시작시킴
        System.out.println("get email ");

        /* 리사이클러 뷰 관련 */
        // 리사이클러 뷰를 만든다
        recyclerView = view.findViewById(R.id.recycler_memo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // 메모 아답터를 생성해준다
        System.out.println("make adapter");
        System.out.println("feed size : " + feed_list.size());
        memo_adapter = new Memo_Adapter(feed_list , getActivity());

        // 리사이클러 뷰에 대해 아답터를 설정해준다.
        recyclerView.setAdapter(memo_adapter);


        return view;
    }


    // 현재 DB에 있는 모든 파일을 휴대폰으로 가져온다
    public class get_email extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);

                    // 연결을 해준다
                    con = (HttpURLConnection) url.openConnection();

                    // 연결 설정해주기
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Cache-Control", "no-cache");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept", "text/html");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.connect();

                    // 서버로 보낼 스트림 -> 이걸 이용한 버퍼 생성
                    OutputStream outStream = con.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    Log.d("\"우한우한우한\"", "doInBackground: " + "{\"my_email\":\""+current_email+"\"}");
                    writer.write("{\"my_email\":\""+current_email+"\"}");
                    writer.flush();
                    writer.close();

                    // 서버로부터 데이터 받음 -> 이걸 이용한 버퍼 생성
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    // 결과로 리턴 될 버퍼
                    StringBuffer buffer = new StringBuffer();

                    // 버퍼 리더로부터 문자열을 받은걸 결과 버퍼에 담는다
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    String _path = "{\"phone_number\":["+buffer.toString().substring(1, buffer.toString().length()-1)+"]}";

                    // phoneArray 만드는 과정
                    JSONObject jsonObject = new JSONObject(_path);
                    allArray = jsonObject.getJSONArray("phone_number");


                    Log.d("여기에요 선생님", "doInBackground: >>>>>>>>>>>>>>>>>>" + allArray.getJSONObject(1).getString("friend_email")+"싸이즈 : " + allArray.length());

                    for(int i=0;i<allArray.length();i++){
                        Log.d("start" , "#" + Integer.toString(i));
                        String item = allArray.getJSONObject(i).getString("friend_email");
                        friend_email_list.add(i, item);
                        Log.d("리스트임.........", "doInBackground: >>>>>>>>>> " + allArray.getJSONObject(i).getString("friend_email"));
                    }




                    System.out.println("친구 몇명 : " + friend_email_list.size());
                    for(int i=0 ; i<friend_email_list.size() ; i++) {
                        System.out.println("친구 이메일" + friend_email_list.get(i));
                        get_feed(friend_email_list.get(i));
                    }

                    // 결과 버퍼 내용을 문자열로 바꿔서 리턴한다
                    return buffer.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally
                {

                    if (con != null) {
                        con.disconnect();
                    }
                    try
                    {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }


    public void get_friend_pic()  {
        for(int i=0;i<allArray.length();i++){
            try {
                one_friend_email = allArray.getJSONObject(i).getString("friend_email");
            }
            catch (JSONException e){
                Log.d("ㅁㄴㅇ", "get_friend_pic: 사진 자체가 아예 없는데?? >>>>>");
            }
            new one_friend().execute("http://192.249.19.241:3880/api/imagelist/");//AsyncTask 시작시킴
        }
    }

    // 찾고싶은 전화번호를 검색한다
    public class one_friend extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    String query = urls[0] + one_friend_email;
                    URL url = new URL(query);
                    Log.d("\"JSONTask3 에러 찾기\"", "doInBackground: >>>>>>>>>>>" + query);
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();

                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    Log.d("\"선생님 여기에요\"2", "doInBackground: >>>>>>>>>>>>>" + buffer.toString());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String tmp_str = buffer.toString().substring(1, buffer.toString().length()-1);
                            try {
                                JSONObject obj = new JSONObject(tmp_str);

                                Toast toast = Toast.makeText(getActivity(), obj.get("friend_phone").toString(), Toast.LENGTH_LONG);
                                toast.show();
                            }catch (JSONException e){
                                e.printStackTrace();
                            }

                        }
                    });
                    return buffer.toString();
                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
}