package com.example.myapplication;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class GalleryItem {
    private String iv;

    public void setIcon(String icon) {
        this.iv = icon;
    }

    public String getIcon(){
        return this.iv;
    }
}
