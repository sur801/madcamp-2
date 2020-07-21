package com.example.myapplication.ui.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.LoginCallback;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.Telephone;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


public class LoginActivity extends AppCompatActivity {
    // facebook login
    private LoginButton btn_facebook_login;
    private LoginCallback mLoginCallback;
    private CallbackManager mCallbackManager;

    // 입력받을 내용 & 버튼
    EditText _email;
    EditText _password;
    Button _button_login;
    Button _button_signin;

    // 임시로 담아둘 string
    String tmp_pwd;
    String tmp_email;
    String current_email;

    // 로그인 or 회원가입 여부 flag
    int flag_login_signup = 0;
    static int val_login = 1;
    static int val_signup = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AccessToken accessToken=AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if(isLoggedIn){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // 버튼 설정
        _email = (EditText)findViewById(R.id.login_signin_email);
        _password = (EditText)findViewById(R.id.login_signin_pwd);
        _button_login = (Button)findViewById(R.id.button_login);
        _button_signin = (Button)findViewById(R.id.button_signin);

        // 페이스북 버튼 설정
        mCallbackManager = CallbackManager.Factory.create();
        mLoginCallback = new LoginCallback();
        btn_facebook_login = (LoginButton) findViewById(R.id.login_button);
        btn_facebook_login.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));



        btn_facebook_login.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    String email = object.getString("email");
                                    System.out.println("email : " + email);

                                    // 현재 이메일 임시로 저장해두기
                                    SharedPreferences sharedPreferences = getSharedPreferences("loginFile",Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("tmp_email", email);
                                    editor.commit();

                                    String test = sharedPreferences.getString("tmp_email", null);
                                    System.out.println("test ! : " + test);


                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();



                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String birthday = object.getString("birthday"); // 01/31/1980 format
                                    System.out.println("birthday : " + birthday);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                Log.e("Callback :: ", "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Callback :: ", "onError : " + error.getMessage());
            }
        });



        // 로그인 버튼 설정
        _button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tmp_email = _email.getText().toString();
                tmp_pwd = _password.getText().toString();

                flag_login_signup = val_login;
                new Login_Signup().execute("http://192.249.19.241:3880/api/login/");//AsyncTask 시작시킴
            }
        });

        // 회원가입 버튼 설정
        _button_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tmp_email = _email.getText().toString();
                tmp_pwd = _password.getText().toString();

                flag_login_signup = val_signup;
                new Login_Signup().execute("http://192.249.19.241:3880/api/signup/");//AsyncTask 시작시킴
            }
        });
    }

    // 사용자 정보 요청
    public void requestMe(AccessToken token) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.e("result",object.toString());
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        AccessToken accessToken=AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
//
//        if(isLoggedIn){
//
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }
    }

    // db login or signup
    public class Login_Signup extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    URL url = new URL(urls[0]);

                    // 연결 설정
                    con = (HttpURLConnection) url.openConnection();
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

                    // 작성
                    String post_body = "{\"my_email\":\"" + tmp_email + "\",\"my_pwd\" :\"" + tmp_pwd + "\"}";
                    writer.write(post_body);
                    writer.flush();
                    writer.close();

                    // 받아오기
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    if(flag_login_signup == val_login) {

                        int tmp_len = buffer.toString().length();
                        current_email = buffer.toString().substring(1, tmp_len-1);
                    }
                    else if(flag_login_signup == val_signup)
                        current_email = tmp_email;

                    // 현재 이메일 임시로 저장해두기
                    SharedPreferences sharedPreferences = getSharedPreferences("loginFile",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("tmp_email", current_email);
                    editor.commit();
                    System.out.println("cur " + current_email);
                    System.out.println("response : "+buffer.toString());

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();

                    return buffer.toString();
                } catch (FileNotFoundException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this,"Wrong email or password", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
                catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d("asdasd", "doInBackground: >>>>>>>>>>22222 : ");
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
                        Log.d("asdasd", "doInBackground: >>>>>>>>>>3 : ");
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