package com.example.myapplication;
import com.google.gson.annotations.SerializedName;
public class ImageInfo {
    //    private List<Date> date;
    private boolean is_edited;
    @SerializedName("my_name")
    private String email;
    @SerializedName("_id")
    private String id;
    private String name;
    private String title;
    private String review;
    private String likes;
    private String rate;
    @SerializedName("_v")
    private int v;

    public ImageInfo(boolean is_edited, String email, String id, String name, String review, String likes, String rate, String title, int v) {

        this.is_edited = is_edited;
        this.email = email;
        this.id = id;
        this.name = name;
        this.review = review;
        this.likes = likes;
        this.rate = rate;
        this.v = v;
    }

    public String getLikes() {
        return likes;
    }

    public String getTitle() {
        return title;
    }

    public String getRate() {
        return rate;
    }

    public boolean isIs_edited() {
        return is_edited;
    }
    public String getId() {
        return id;
    }

    public String getReview() {
        return review;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public int getV() {
        return v;
    }
}