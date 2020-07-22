package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

public class FreeTabFragment extends Fragment {
    public FreeTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_freetab, container, false);

        VideoView videoView = view.findViewById(R.id.video_view);
        String videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.summer_hate;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        // create an object of media controller
        MediaController mediaController = new MediaController(getActivity());
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        return view;
    }
}
