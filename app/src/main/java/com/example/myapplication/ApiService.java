package com.example.myapplication;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

interface ApiService {

    @Multipart
    @POST("upload")
    Call<ResponseBody> postImage(@Part MultipartBody.Part image, @Part("email") RequestBody email ,@Part("name") RequestBody name, @Part("review") RequestBody review, @Part("title") RequestBody title, @Part("rate") RequestBody rate );

    @GET("imagelist/{email}")
    Call<List<ImageInfo>> getList(@Path("email") String email);

    @GET("imagedelete/{imageName}")
    Call<ResponseBody> deleteImage(@Path("imageName") String imageName);

    @POST("addressbook/phone_all")
    Call<ResponseBody> loadFriends(@Part("my_email") String email);


}
