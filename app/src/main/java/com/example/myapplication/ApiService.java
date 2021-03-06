package com.example.myapplication;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface ApiService {

    @Multipart
    @POST("upload")
    Call<ResponseBody> postImage(@Part MultipartBody.Part image, @Part("email") RequestBody email ,@Part("name") RequestBody name, @Part("review") RequestBody review, @Part("title") RequestBody title, @Part("rate") RequestBody rate );

    @GET("imagelist/{email}")
    Call<List<ImageInfo>> getList(@Path("email") String email);

    @GET("imagedelete/{imageName}")
    Call<ResponseBody> deleteImage(@Path("imageName") String imageName);

    @GET("update/{imageName}")
    Call<ResponseBody> updateImage(@Path("imageName") String imageName);

//    @POST("addressbook/phone_all")
//    Call<ResponseBody> loadFriends(@Part("my_email") String email);

    @Headers({"X-Naver-Client-Id: 8rWjzevXr3CgLcLCfzgZ", "X-Naver-Client-Secret: Sbe9xBRC7c"})
    @GET("movie.json")
    Call<MovieInfo> loadMovie(@Query("query") String query, @Query("display") String display,  @Query("start") int startPosition);


}
