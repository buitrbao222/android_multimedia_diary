package com.example.multimedia_diary;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;

public class EntryDetailsActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_details);

        // Enable back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        DiaryEntry entry = (DiaryEntry) getIntent().getSerializableExtra("entry");

        setTitle(entry.createdAt);

        // Initialize weather emoji
        TextView weatherEmojiTextView = findViewById(R.id.details_weather_emoji);
        weatherEmojiTextView.setText(entry.weatherEmoji);

        // Initialize content
        TextView contentTextView = findViewById(R.id.details_content);
        contentTextView.setText(entry.content);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (entry.latitude != 0 && entry.longitude != 0) {
            // Set marker when map is ready
            mapFragment.getMapAsync(googleMap -> {
                LatLng location = new LatLng(entry.latitude, entry.longitude);
                googleMap.addMarker(new MarkerOptions().position(location));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            });
        } else {
            mapFragment.getView().setVisibility(View.GONE);
        }

        // Initialize image grid view
        ImageLoader imageLoader = ImageLoader.getInstance();

        ArrayList<Bitmap> imageBitmaps = new ArrayList<>();
        GridView imageGridView = findViewById(R.id.details_image_grid_view);
        ImageListAdapter imageListAdapter = new ImageListAdapter(this, R.layout.image_list_item, imageBitmaps);

        imageGridView.setAdapter(imageListAdapter);

        File photosDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] photos = photosDir.listFiles();
        for (File photo : photos) {
            if (photo.getName().startsWith(entry.id)) {
                imageLoader.loadImage(String.valueOf(Uri.fromFile(photo)), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageBitmaps.add(loadedImage);
                        imageListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        // Get video from external directory
        File videosDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File[] videos = videosDir.listFiles();

        // Initialize video view
        for (File video : videos) {
            if (video.getName().startsWith(entry.id)) {
                LinearLayout videoViewLayout = findViewById(R.id.details_video_layout);
                videoViewLayout.setVisibility(View.VISIBLE);

                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(videoViewLayout);

                VideoView videoView = findViewById(R.id.details_video);

                Uri videoUri = Uri.fromFile(video);
                videoView.setVideoURI(videoUri);
                videoView.setMediaController(mediaController);
                break;
            }
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}