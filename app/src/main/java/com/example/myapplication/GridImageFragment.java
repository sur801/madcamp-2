package com.example.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.myapplication.MainActivity.REQUEST;


// intent를 이용해서 사진을 찍고, 해당 사진을 external primal directory에 저장한다
// 해당 사진들을 grid imgage를 이용해서 여러 장 표현한다
public class GridImageFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "GridImage";

    public GridImageFragment() {
        // Required empty public constructor
    }

    View mView;
    // recycler object
    RecyclerView _recycle_image;
    // adapter object
    GalleryAdapter gallery_adapter = null;

    // for get current email
    SharedPreferences sharedPreferences;

    // array list for permission
    private ArrayList<String> permissions = new ArrayList<>();
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 107;

    // buttons for uploading image
    FloatingActionButton fabCamera, fabUpload;

    // image variabled for upload images
    Bitmap mBitmap;
    String mFilePath;
    private final static int IMAGE_RESULT = 200;
    Uri picUri;

    // object for use api service
    ApiService apiService;
    public List<ImageInfo> imageList=null;

    // 리뷰 작성하기
    String review_content;
    String review_title;
    String review_rate;

    // refresh fragment when image data updated
    public void refreshFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.detach(this).attach(this).commit();
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (ContextCompat.checkSelfPermission(getContext(),permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }


    // ask permission
    private void askPermissions() {
        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    // get imagelist from db
    public void getImageList(View view) {
        System.out.println("getImageList START");
        //get my current email
        sharedPreferences = getActivity().getSharedPreferences("loginFile", Context.MODE_PRIVATE);
        String cur_email = sharedPreferences.getString("tmp_email", null);

        Call<List<ImageInfo>> req2 = apiService.getList(cur_email);
        req2.enqueue(new Callback<List<ImageInfo>>() {
            @Override
            public void onResponse(Call<List<ImageInfo>> call, Response<List<ImageInfo>> response) {
                System.out.println("getImageList MID1");

                // image list
                imageList = response.body();
                setRecycleView(view);

            }

            @Override
            public void onFailure(Call<List<ImageInfo>> call, Throwable t) {
                System.out.println("getImageList MID2");
                System.out.println(call.toString());
                System.out.println(t.toString());
            }
        });

        System.out.println("getImageList END");
    }

    public void syncImageList() {

        //get my current email
        sharedPreferences = getActivity().getSharedPreferences("loginFile", Context.MODE_PRIVATE);
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
                int originSize = imageList.size();

                if(tempSize != originSize) {
                    if(tempSize > originSize) {
                        for(int i = originSize ; i<tempSize ; i++) {
                            imageList.add(tempImageList.get(i));
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<List<ImageInfo>> call, Throwable t) {
                System.out.println("getImageList MID2");
                System.out.println(call.toString());
                System.out.println(t.toString());
            }
        });

        System.out.println("getSync End");
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
    private void setRecycleView(View view){
        /*connect to recycler view*/
        _recycle_image = (RecyclerView) view.findViewById(R.id.recycle_image);

        gallery_adapter = new GalleryAdapter(imageList, getContext(), GridImageFragment.this);
        _recycle_image.setAdapter(gallery_adapter);

        // (3) 레이아웃 매니저를 설정해준다.
        // 리사이클러 뷰 안에 있는 사진들을 일정한 크기별로 표현해주기 위해 그리드 레이아웃 사용
        GridLayoutManager gm = new GridLayoutManager(getActivity(), 3);

        _recycle_image.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 현재 view(grid view)를 inflate 해서 main view로 올려준다
        mView = inflater.inflate(R.layout.fragment_gallery, container, false);

        fabCamera = mView.findViewById(R.id.fab);
        fabUpload = mView.findViewById(R.id.fabUpload);
        fabCamera.setOnClickListener(this);
        fabUpload.setOnClickListener(this);

        askPermissions();
        initRetrofitClient();
        getImageList(mView);

        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 뒤로가기 버튼 눌렀을 때
//                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }




    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getActivity().getExternalFilesDir("");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_RESULT) {

                // 보내는 file path
                mFilePath = getImageFilePath(data);
                if (mFilePath != null) {
                    System.out.println(mFilePath);
                    mBitmap = BitmapFactory.decodeFile(mFilePath);
                    Toast.makeText(getActivity(), mFilePath, Toast.LENGTH_SHORT).show();
                    if(mBitmap==null) Toast.makeText(getActivity(), "bitmap null", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getActivity(), "get photo successfully", Toast.LENGTH_SHORT).show();

                    write_review();
                }
            }
        }
    }

    // 사진 찍은 영화에 대해 리뷰를 작성한다.
    public void write_review(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.review_box, null);
        builder.setView(view);

        final Button submit = (Button)view.findViewById(R.id.review_submit);
        final EditText _title = (EditText)view.findViewById(R.id.edittext_title);
        final EditText _content = (EditText)view.findViewById(R.id.edittext_content);
        final EditText _rate = (EditText)view.findViewById(R.id.edittext_rate);
        final ImageView _img = (ImageView)view.findViewById(R.id.moview_img);
        Glide.with(getActivity()).load(mBitmap).into(_img);

        final android.app.AlertDialog dialog = builder.create();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                review_title = _title.getText().toString();
                review_content = _content.getText().toString();
                review_rate = _rate.getText().toString();

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;

        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return getPathFromURI(data.getData());

    }

    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }
    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("pic_uri", picUri);
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
        }
    }

    public Intent getPickImageChooserIntent() {
        // image choosing intent

        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getActivity().getPackageManager();

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    private void multipartImageUpload() {
        try {
            File filesDir = getActivity().getFilesDir();
            File file = new File(filesDir, "image" + ".png");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();


            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();


            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(),reqFile);
            System.out.println("file path : "+mFilePath);
            RequestBody pathName = RequestBody.create(MediaType.parse("text/plain"), mFilePath);
            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload");

            //get my current email
            sharedPreferences = getActivity().getSharedPreferences("loginFile", Context.MODE_PRIVATE);
            String cur_email = sharedPreferences.getString("tmp_email", null);
            RequestBody email = RequestBody.create(MediaType.parse("text/plain"), cur_email);

            RequestBody review = RequestBody.create(MediaType.parse("text/plain"), review_content);
            // 수정 필요함
            RequestBody title = RequestBody.create(MediaType.parse("text/plain"), review_title);
            RequestBody rate = RequestBody.create(MediaType.parse("text/plain"), review_rate);

            // for image upload
            Call<ResponseBody> req = apiService.postImage(body, email, name,review, title, rate);

            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.code() == 200 || response.code() == 404) {
                        Toast.makeText(getActivity(), "Upload Successfully", Toast.LENGTH_SHORT).show();
                        int size = imageList.size();
                        syncImageList();
                        //System.out.println(size);
                        //gallery_adapter.notifyItemInserted(size);
                        refreshFragment();



                    }

                    //Toast.makeText(getActivity(), response.code() + " ", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getActivity(), "Request failed", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
                break;

            case R.id.fabUpload:
                if (mBitmap != null)
                    multipartImageUpload();
                else {
                    Toast.makeText(getActivity(), "Bitmap is null. Try again", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}