package com.example.multimedia_diary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;

public class EntryDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_details);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        DiaryEntry entry = (DiaryEntry) getIntent().getSerializableExtra("entry");

        setTitle(entry.createdAt);

        // Initialize weather emoji
        TextView weatherEmojiTextView = findViewById(R.id.details_weather_emoji);
        weatherEmojiTextView.setText(entry.weatherEmoji);

        // Initialize content
        TextView contentTextView = findViewById(R.id.details_content);
        contentTextView.setText(entry.content);

        if (entry.latitude != 0 && entry.longitude != 0) {
            TextView latitudeTextView = findViewById(R.id.details_latitude);
            latitudeTextView.setText("Latitude: " + entry.latitude);
            latitudeTextView.setVisibility(View.VISIBLE);

            TextView longitudeTextView = findViewById(R.id.details_longitude);
            longitudeTextView.setText("Longitude: " + entry.longitude);
            longitudeTextView.setVisibility(View.VISIBLE);
        }


        // Get photos from external directory
        ArrayList<Bitmap> imageBitmaps = new ArrayList<>();
        File photosDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] photos = photosDir.listFiles();
        for (File photo : photos) {
            if (photo.getName().startsWith(entry.id)) {
                // Get bitmap from image
                Bitmap bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
                imageBitmaps.add(bitmap);
            }
        }

        // Initialize image grid view
        GridView imageGridView = findViewById(R.id.details_image_grid_view);
        ImageListAdapter imageListAdapter = new ImageListAdapter(this, R.layout.image_list_item, imageBitmaps);
        imageGridView.setAdapter(imageListAdapter);

        // Get videos from external directory
        ArrayList<Uri> videoUris = new ArrayList<>();
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