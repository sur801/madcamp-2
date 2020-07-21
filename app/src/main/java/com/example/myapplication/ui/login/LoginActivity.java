package com.example.myapplication.ui.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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
import java.io.DataOutputStream;
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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


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

    // 임시로 담아둘 string - 로그인 용
    String tmp_pwd;
    String tmp_email;

    // 임시로 담아둘 string - 회원가입 용
    String sign_email;
    String sign_pwd;
    String sign_name;
    String sign_rand;
    String sign_phone_num;
    String current_email;

    // 로그인 or 회원가입 여부 flag
    int flag_login_signup = 0;
    static int val_login = 1;
    static int val_signup = 2;

    // 회원가입용 인증번호 4자리
    int rand_register;

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
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                sign_up();

//                flag_login_signup = val_signup;
//                new Login_Signup().execute("http://192.249.19.241:3880/api/signup/");//AsyncTask 시작시킴
            }
        });
    }

    // 인증번호 전달하기
    public class Request {
        public void main(String parameters) throws Exception {
            String targetUrl = "http://api.solapi.com/messages/v4/send";
//            String parameters = "{\"message\":{\"to\":\"01055132946\",\"from\":\"01072027518\",\"text\":\"내용\"}}";

            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");

            con.setRequestProperty("Authorization", "HMAC-SHA256 apiKey=NCSAYU7YDBXYORXC, date=2019-07-01T00:41:48Z, salt=jqsba2jxjnrjor, signature=1779eac71a24cbeeadfa7263cb84b7ea0af1714f5c0270aa30ffd34600e363b4");
            con.setRequestProperty("Content-Type", "application/json");

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            System.out.println("HTTP response code : " + responseCode);
            System.out.println("HTTP body : " + response.toString());
        }
    }

    // 회원가입용 alert dialog
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void sign_up()  {
//        // 테스트용
//        Request req = new Request();
//        String tmp_json;
//        tmp_json = "{\"message\":{\"to\":\"01000000001\",\"from\":\"029302266\",\"text\":\"내용\"}}";
//        try {
//            req.main(tmp_json);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.sign_up_box, null);
        builder.setView(view);

        final Button submit = (Button)view.findViewById(R.id.sign_submit);
        final Button sms_get = (Button)view.findViewById(R.id.button5);
        final EditText _my_name = (EditText)view.findViewById(R.id.signName_write);
        final EditText _my_phone = (EditText)view.findViewById(R.id.signPhone_write);
        final EditText _my_email = (EditText)view.findViewById(R.id.signEmail_write);
        final EditText _my_pwd = (EditText)view.findViewById(R.id.signPwd_write);
        final EditText _rand = (EditText)view.findViewById(R.id.random_register);
        final  AlertDialog dialog = builder.create();

        sms_get.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sign_phone_num = _my_phone.getText().toString();

                rand_register = new Random().nextInt(9000) + 1000;
                Log.d("ㅁㄴㅇㅁㄴㅇ", "sign_up: >>>>>>>>>>" + rand_register);

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("" + sign_phone_num, null, "인증번호 입니다 : [ " + rand_register + " ]", null, null);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sign_name = _my_name.getText().toString();
                sign_phone_num = _my_phone.getText().toString();
                sign_email = _my_email.getText().toString();
                sign_pwd = _my_pwd.getText().toString();
                sign_rand = _rand.getText().toString();

                dialog.dismiss();

                if(rand_register == Integer.parseInt(sign_rand)) {
                    flag_login_signup = val_signup;
                    new Login_Signup().execute("http://192.249.19.241:3880/api/signup/");//AsyncTask 시작시킴
                }else{
                    Log.d("ㅁㄴㅇㅁㄴㅇ", "onClick: >>>>>>>>>>>" + rand_register +"..."+ Integer.parseInt(sign_rand));
                    Toast.makeText(LoginActivity.this, "인증번호를 잘못 입력하셨습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
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
                    String post_body = "{\"my_email\":\"" + sign_email + "\",\"my_pwd\" :\"" + sign_pwd + "\", \"my_name\":\""+sign_name+"\",\"my_phone_num\":\""+sign_phone_num+"\"}";
                    Log.d("야 ", "로그인 신호 어떻게 보내냐: >>>>" + post_body);
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