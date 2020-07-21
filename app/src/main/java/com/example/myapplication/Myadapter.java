package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class Myadapter extends FragmentPagerAdapter {
    int totalTabs;

    public GridImageFragment gridImage;
    public Myadapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Telephone telephone = new Telephone();
                return telephone;
            case 1:
                gridImage = new GridImageFragment();
                return gridImage;
//            case 2:
//                MemoFragment memo = new MemoFragment();
//                return memo;
            default:
                return null;
        }
    }

        // counts total number of tabs
    @Override
        public int getCount() {
        return 2;
    }

//    public int getCount() {
//        return 3;
//    }
}
