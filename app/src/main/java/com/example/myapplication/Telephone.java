package com.example.myapplication;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ui.login.LoginActivity;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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

public class Telephone extends Fragment implements View.OnClickListener{
    ArrayList<phone> phoneList = new ArrayList<phone>();

    public Telephone() {
        // Required empty public constructor
    }

    ArrayList<phone> datas = new ArrayList<phone>();

    // 초기 전화번호부
    int upload_flag = 0; // 초기 딱 한 번만 JSON & 휴대폰 파일을 가져오기 위함. 그 뒤로는 안 가져오게 할거임.
    int last_size = 0;
    int last_update_idx = 0;

    // 전체 레이아웃
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    MyAdapter myAdapter;

    // 저장용 (all)
    FloatingActionButton btn;
    JSONArray phoneArray;

    // 호출용 (all)
    JSONArray allArray;

    // 호출용 (one)
    FloatingActionButton btn3;
    String find_name;
    String find_phone;

    // 삭제용 (one)
    FloatingActionButton btn4;
    String del_name;
    String del_phone;

    // 단말기 번호 DB로 연동
    String my2db; // json 담기 위함
    JSONArray my_phoneArray;

    // 추가용 (one)
    FloatingActionButton btn6;
    String write_name;
    String write_phone_num;
    String write_friend_email;

    // tracking fb login token
    AccessTokenTracker accessTokenTracker;

    // 현재 내 이메일
    String current_email;

    // floating action button을 위함
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    FloatingActionButton main_fab_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_telephone, container, false);

        // 애니메이션 설정을 해주기 위함
        fab_open = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_close);

        // 현재 내 이메일을 가져온다
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginFile",Context.MODE_PRIVATE);
        current_email = sharedPreferences.getString("tmp_email", null);
        System.out.println("current_email ! : " + current_email+"}");

        // 만든 phoneList를 이용 -> 첫 번째 tab 초기화면부터 완성
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager); // LayoutManager 등록
        myAdapter = new MyAdapter(phoneList);
        recyclerView.setAdapter(myAdapter);  // Adapter 등록

        getContacts(getActivity());

        // 버튼 설정
        main_fab_btn = (FloatingActionButton)view.findViewById(R.id.main_floating);
        btn = (FloatingActionButton)view.findViewById(R.id.button);
        btn3 = (FloatingActionButton)view.findViewById(R.id.button3);
        btn4 = (FloatingActionButton)view.findViewById(R.id.button4);
        btn6 = (FloatingActionButton)view.findViewById(R.id.button6);
        main_fab_btn.setOnClickListener(this);
        btn.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn6.setOnClickListener(this);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    //write your code here what to do when user clicks on facebook logout
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        };
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            /* 휴대폰 -> DB : (current_path + 친구 email) 둘다 기록해줘야 함
               DB -> 휴대폰 : (current_path) 이용해서 가져와야 함
            */
            case R.id.main_floating:
                anim();
                break;
            case R.id.button:
                // 휴대폰 JS + 주소록 -> DB등록 -> 휴대폰으로 가져오기
                anim();
                phoneBook_sync();
                break;
            case R.id.button3:
                // DB -> 휴대폰 하나 찾아오기 (Post 방식) : 검색하기 위해 current_path 필요
                anim();
                show_find_box();
                break;
            case R.id.button4:
                // DB 전화번호 하나 삭제 (Post 방식) : current_path 이용해서 찾아야
                anim();
                show_delete_box();
                break;
            case R.id.button6:
                // 휴대폰 주소 하나 입력 -> DB (Post 방식) : current_path 를 첨가해서 DB에 보낸다 + 친구 email도 추가해줘야
                anim();
                show_write_box();
                break;

                // JSONTask5, 6은 동일한 라우터를 호출하지만, 몇 개를 호출하는지가 다르므로 따로 함수를 나눠줬다.
        }
    }

    public void anim() {
        if (isFabOpen) {
            btn.startAnimation(fab_close);
            btn3.startAnimation(fab_close);
            btn4.startAnimation(fab_close);
            btn6.startAnimation(fab_close);
            btn.setClickable(false);
            btn3.setClickable(false);
            btn4.setClickable(false);
            btn6.setClickable(false);
            isFabOpen = false;
        } else {
            btn.startAnimation(fab_open);
            btn3.startAnimation(fab_open);
            btn4.startAnimation(fab_open);
            btn6.startAnimation(fab_open);
            btn.setClickable(true);
            btn3.setClickable(true);
            btn4.setClickable(true);
            btn6.setClickable(true);
            isFabOpen = true;
        }
    }

    // 주소록 싱크를 맞춘다 (download & load)
    public void phoneBook_sync(){
        if(upload_flag == 0) {
            /* 휴대폰 내부 JSON -> DB 다운로드*/
            // 휴대폰에 있는 JSON 가져와서 phoneList 만들기 위함
            String json_str = getJsonString();
            jsonParsing(json_str, 1);
            new JSONTask().execute("http://192.249.19.241:3880/api/addressbook/");//AsyncTask 시작시킴

            /*휴대폰 주소록 -> DB 다운로드*/
            new JSONTask5().execute("http://192.249.19.241:3880/api/addressbook/");//AsyncTask 시작시킴

            upload_flag = 1;
        }
        /*DB -> 휴대폰 업로드*/
        new JSONTask2().execute("http://192.249.19.241:3880/api/addressbook/phone_all");//AsyncTask 시작시킴
    }

    // 전화번호 하나를 DB에 저장하려 함
    public void show_write_box(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.write_box, null);
        builder.setView(view);

        final Button submit = (Button)view.findViewById(R.id.write_submit);
        final EditText _name = (EditText)view.findViewById(R.id.edittextName_write);
        final EditText _phone = (EditText)view.findViewById(R.id.edittextPhone_write);
        final EditText _friend_email = (EditText)view.findViewById(R.id.edittextEmail_write);

        final  AlertDialog dialog = builder.create();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write_name = _name.getText().toString();
                write_phone_num = _phone.getText().toString();
                write_friend_email = _friend_email.getText().toString();
                new JSONTask6().execute("http://192.249.19.241:3880/api/addressbook/");//AsyncTask 시작시킴
                new JSONTask2().execute("http://192.249.19.241:3880/api/addressbook/phone_all");//AsyncTask 시작시킴
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // 하나 DB에서 삭제하려 함
    public void show_delete_box(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.delete_box, null);
        builder.setView(view);

        final Button submit = (Button)view.findViewById(R.id.del_submit);
        final EditText _name = (EditText)view.findViewById(R.id.edittextName);
        final EditText _phone = (EditText)view.findViewById(R.id.edittextPhone);

        final  AlertDialog dialog = builder.create();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                del_name = _name.getText().toString();
                del_phone = _phone.getText().toString();

                new JSONTask4().execute("http://192.249.19.241:3880/api/addressbook/delete/phone_one");//AsyncTask 시작시킴

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // 하나 DB에서 검색하려 함
    public void show_find_box(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.find_box, null);
        builder.setView(view);

        final Button submit = (Button)view.findViewById(R.id.find_submit);
        final EditText _name = (EditText)view.findViewById(R.id.name_to_phone);

        final  AlertDialog dialog = builder.create();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                find_name = _name.getText().toString();

                // 찾고싶은 전화번호를 검색한다
                new Telephone.JSONTask3().execute("http://192.249.19.241:3880/api/addressbook/phone_one");//AsyncTask 시작시킴

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // 현재 JSON에 있는 모든 파일들을 디비에 저장한다
    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                for (int i = 0; i < phoneArray.length(); i++) {
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

                        String tmp = phoneArray.getJSONObject(i).toString().substring(0, phoneArray.getJSONObject(i).toString().length()-1) + ", \"my_email\":\""+current_email+"\"}";
                        Log.d("JSONTask1 에러 찾기", "doInBackground: >>>>>>>>>" + tmp);
                        writer.write(tmp);
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

                        // 결과 버퍼 내용을 문자열로 바꿔서 리턴한다
                        return buffer.toString();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally
                    {
                        if(i != phoneArray.length()-1)
                            continue;

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

    // 현재 DB에 있는 모든 파일을 휴대폰으로 가져온다
    public class JSONTask2 extends AsyncTask<String, String, String> {
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
                    Log.d("\"JSONTask2 에러 찾기\"", "doInBackground: " + "{\"my_email\":\""+current_email+"\"}");
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
                    jsonParsing(_path,3 );
                    Log.d("\"JSONTask2 에러 찾기2\"", "doInBackground: >>>>>>>>>>>>>>>>" + _path);
                    // DB 내용 리사이클러 뷰 올리기
                    for(int i=last_update_idx ; i < allArray.length() ; i++){
                        JSONObject tmp_object = allArray.getJSONObject(i);

                        phone phone_arr = new phone();

                        phone_arr.setName(tmp_object.getString("friend_name"));
                        phone_arr.setPhone_num(tmp_object.getString("friend_phone"));

                        phoneList.add(phone_arr);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyItemChanged(phoneList.size());
                            }
                        });
                    }
                    last_update_idx = allArray.length();

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

    // 찾고싶은 전화번호를 검색한다
    public class JSONTask3 extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    String query = urls[0] + "?name="+find_name+"&my_email="+current_email;
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
                    Log.d("\"JSONTask3 에러 찾기\"2", "doInBackground: >>>>>>>>>>>>>" + buffer.toString());

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

    // 삭제하고싶은 전화번호를 삭제해준다
    public class JSONTask4 extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    String query = urls[0] + "?name="+del_name+"&phone_num="+del_phone+"&my_email="+current_email;
                    URL url = new URL(query);
                    Log.d("\"JSONTask4 에러 찾기\"", "doInBackground: >>>>>>>>>>>>>"+query);
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();

                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    Log.d("\"JSONTask4 에러 찾기\"2", "doInBackground: >>>>>>>>>>>>>" + buffer.toString());
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

    // 현재 휴대폰에 있는 모든 파일들을 디비에 저장한다
    public class JSONTask5 extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                for (int i = 0; i < my_phoneArray.length(); i++) {
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
                        Log.d("\"JSONTask5 에러 찾기\"", "doInBackground: >>>>>>>>>>>>>>>>>> 이건 또 뭐냐 :"+ my_phoneArray.getJSONObject(i).toString());
                        writer.write(my_phoneArray.getJSONObject(i).toString());
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

                        // 결과 버퍼 내용을 문자열로 바꿔서 리턴한다
                        return buffer.toString();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally
                    {
                        if(i != my_phoneArray.length()-1)
                            continue;

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

    // 입력한 전화번호/이름을 DB에 저장한다
    public class JSONTask6 extends AsyncTask<String, String, String> {
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

                    String tmp = "{\"my_email\":\""+current_email+"\", \"name\":\""+ write_name + "\", \"phone_num\":\"" + write_phone_num + "\", \"friend_email\": \"" + write_friend_email + "\"}";
                    writer.write(tmp);
                    Log.d("\"JSONTask6 에러 찾기\"", "doInBackground: 또 왜>>>>>>>>>>>>>>>" + tmp);
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

    // 휴대폰에서 DB로 전화번호를 옮겨주기 위한 array 전환 : my_phoneArray
    public void getContacts(Context context){
        // 데이터베이스 혹은 content resolver 를 통해 가져온 데이터를 적재할 저장소를 먼저 정의

        // 1. Resolver 가져오기(데이터베이스 열어주기)
        // 전화번호부에 이미 만들어져 있는 ContentProvider 를 통해 데이터를 가져올 수 있음
        // 다른 앱에 데이터를 제공할 수 있도록 하고 싶으면 ContentProvider 를 설정
        // 핸드폰 기본 앱 들 중 데이터가 존재하는 앱들은 Content Provider 를 갖는다
        // ContentResolver 는 ContentProvider 를 가져오는 통신 수단
        ContentResolver resolver = context.getContentResolver();

        // 2. 전화번호가 저장되어 있는 테이블 주소값(Uri)을 가져오기
        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        // 3. 테이블에 정의된 칼럼 가져오기
        // ContactsContract.CommonDataKinds.Phone 이 경로에 상수로 칼럼이 정의
        String[] projection = { ContactsContract.CommonDataKinds.Phone.CONTACT_ID // 인덱스 값, 중복될 수 있음 -- 한 사람 번호가 여러개인 경우
                ,  ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ,  ContactsContract.CommonDataKinds.Phone.NUMBER
                ,  ContactsContract.CommonDataKinds.Email.DATA};

        // 4. ContentResolver로 쿼리를 날림 -> resolver 가 provider 에게 쿼리하겠다고 요청
        Cursor cursor = resolver.query(phoneUri, projection, null, null, null);

        // json 파일 만들어주기 위해 String 형식으로 만든다. 그래야 전달 가능
        my2db = "{ \"phone_number\" : [";

        // 4. 커서로 리턴된다. 반복문을 돌면서 cursor 에 담긴 데이터를 하나씩 추출
        if(cursor != null){
            while(cursor.moveToNext()){
                // 4.1 이름으로 인덱스를 찾아준다
                int idIndex = cursor.getColumnIndex(projection[0]); // 이름을 넣어주면 그 칼럼을 가져와준다.
                int nameIndex = cursor.getColumnIndex(projection[1]);
                int numberIndex = cursor.getColumnIndex(projection[2]);
                int emailIndex = cursor.getColumnIndex(projection[3]);

                // 4.2 해당 index 를 사용해서 실제 값을 가져온다.
                String age = cursor.getString(idIndex);
                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);
                String email = cursor.getString(emailIndex);

                phone phoneBook = new phone();
                phoneBook.setName(name);
                phoneBook.setPhone_num(number);

                datas.add(phoneBook);

                my2db = my2db + "{ \"name\" : \""+name+"\"," + "\"phone_num\" : \"" + number + "\", \"my_email\" : \"" + current_email + "\", \"friend_email\" : \"" + email + "\"},";
            }
        }
        my2db = my2db.substring(0, my2db.length()-1);
        my2db = my2db +"]}";
        Log.d("\"getcontacts 에러 찾기\"", "getContacts: >>>>>>>>>>>>>>> 여기지롱: " + my2db);
        try {
            // my_phoneArray 만드는 과정
            JSONObject jsonObject = new JSONObject(my2db);

            // initialize from asset
            my_phoneArray = jsonObject.getJSONArray("phone_number");

            Log.d("ㅁㄴㅇㅁㄴㅇㅁㄴㅇ", "개빡세누: >>>>>>>>>>>>>>>>>> " + my2db);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 데이터 계열은 반드시 닫아줘야 한다.
        cursor.close();
    }

    // json 파싱용 클래스
    public class phone {
        private String name;
        private String phone_num;
        private String email;

        public String getName() {
            return name;
        }

        public String getPhone_num() { return phone_num; }

        public String get_email() { return email; }

        public void setName(String _name) {
            this.name = _name;
        }

        public void setPhone_num(String _phone_num) {
            this.phone_num = _phone_num;
        }

        public void set_email(String _email) {
            this.email = _email;
        }
    }

    //json -> str 변환용 (휴대폰 내부 JSON을 단말기로 옮기기 위함)
    private String getJsonString() {
        String json = "";

        try {
            InputStream is = getContext().getAssets().open("phone_number.json");
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
            Log.d("여기일까요ㅕ", ">>>>>>>>>>>>>>>>>>>> " + json);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return json;
    }

    // json 실제 파싱 (휴대폰 JSON -> DB  또는  휴대폰 단말기 JSON -> DB 두 가지)
    private void jsonParsing(String json,int give_get) {
        try {
            // phoneArray 만드는 과정
            JSONObject jsonObject = new JSONObject(json);

            // 휴대폰 JSON -> DB
            if(give_get == 1){
                phoneArray = jsonObject.getJSONArray("phone_number");

                // 리사이클러 뷰로 올리는 과정
                for (int i = 0; i < phoneArray.length(); i++) {
                    JSONObject movieObject = phoneArray.getJSONObject(i);

                    phone phone_arr = new phone();

                    phone_arr.set_email(movieObject.getString("friend_email"));
                    phone_arr.setName(movieObject.getString("name"));
                    phone_arr.setPhone_num(movieObject.getString("phone_num"));

                    phoneList.add(phone_arr);
                }
            }
            // get from DB
            else if(give_get==3) {
                allArray = jsonObject.getJSONArray("phone_number");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // tab1 - recyclerview 어답터
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private ArrayList<phone> myDataList = null;

        MyAdapter(ArrayList<phone> dataList) {
            myDataList = dataList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //전개자(Inflater)를 통해 얻은 참조 객체를 통해 뷰홀더 객체 생성
            View view = inflater.inflate(R.layout.cardview, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            //ViewHolder가 관리하는 View에 position에 해당하는 데이터 바인딩
            viewHolder.name.setText(myDataList.get(position).getName());
            viewHolder.phone_num.setText(myDataList.get(position).getPhone_num());
            viewHolder.image.setImageResource(R.drawable.character2);
            //viewHolder.age.setText(myDataList.get(position).getAge());
        }

        @Override
        public int getItemCount() {
            //Adapter가 관리하는 전체 데이터 개수 반환
            return myDataList.size();
        }

        // tab1 - recyclerview : view holder
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView name;
            TextView phone_num;
            ImageView image;
            ImageView deleteImageIcon;
            ImageView sms;
            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                phone_num = itemView.findViewById(R.id.phone_num);
                image = itemView.findViewById(R.id.imageView5);
                sms = itemView.findViewById(R.id.image_sms);

                deleteImageIcon = itemView.findViewById(R.id.image_delete);
                deleteImageIcon.setOnClickListener(this);
                sms.setOnClickListener(this);
            }
            @Override
            public void onClick(View view) {
                if (view.equals(deleteImageIcon)) {
                    removeAt(getAdapterPosition());
                } else if (view.equals(sms)) {
                    Toast.makeText(getContext(), "메세지를 보낼 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void removeAt(int position) {
            myDataList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, myDataList.size());
        }
    }

    // 주소록 adapter
    public class MyAdapter2 extends RecyclerView.Adapter<Telephone.MyAdapter2.ViewHolder> {

        private ArrayList<Telephone.phone> myDataList = null;

        MyAdapter2(ArrayList<Telephone.phone> dataList)
        {
            myDataList = dataList;
        }

        @Override
        public Telephone.MyAdapter2.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //전개자(Inflater)를 통해 얻은 참조 객체를 통해 뷰홀더 객체 생성
            View view = inflater.inflate(R.layout.cardview, parent, false);
            Telephone.MyAdapter2.ViewHolder viewHolder = new Telephone.MyAdapter2.ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull Telephone.MyAdapter2.ViewHolder holder, int position) {
            holder.name.setText( myDataList.get(position).getName());
            holder.phone_num.setText(myDataList.get(position).getPhone_num());
            holder.image.setImageResource(R.drawable.character);
        }

        @Override
        public int getItemCount()
        {
            //Adapter가 관리하는 전체 데이터 개수 반환
            return myDataList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView name;
            TextView phone_num;
            ImageView image;
            ImageView deleteImageIcon;
            ImageView sms;

            //TextView age;

            ViewHolder(View itemView)
            {
                super(itemView);

                name = itemView.findViewById(R.id.name);
                phone_num = itemView.findViewById(R.id.phone_num);
                image = itemView.findViewById(R.id.imageView5);
                sms = itemView.findViewById(R.id.image_sms);

                deleteImageIcon = itemView.findViewById(R.id.image_delete);
                deleteImageIcon.setOnClickListener(this);
                sms.setOnClickListener(this);
                //age = itemView.findViewById(R.id.age);
            }

            @Override
            public void onClick(View view) {
                if (view.equals(deleteImageIcon)) {
                    Toast toast = Toast.makeText(getContext(),"delete a contract", Toast.LENGTH_SHORT);
                    toast.show();
                    removeAt(getAdapterPosition());
                } else if (view.equals(sms)) {
                    // sms 앱과 연결하기 전 토스트로 연결할게 알려준다
                    Toast toast = Toast.makeText(getContext(),"connect with sms", Toast.LENGTH_SHORT);
                    toast.show();
                    // 해당 번호를 변수에 담는다
                    String phoneNo = phone_num.getText().toString();
                    try {
                        // 전송하기
                        final EditText editText = new EditText(getActivity());
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Send Message");
                        builder.setMessage("원하는 문자 내용을 입력하세요");
                        builder.setView(editText);
                        builder.setPositiveButton("입력", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(phoneNo, null, editText.getText().toString(), null, null);
                                Toast.makeText(getContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getContext(), "취소하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "전송 실패!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        }

        public void removeAt(int position) {
            myDataList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, myDataList.size());
        }
    }
}