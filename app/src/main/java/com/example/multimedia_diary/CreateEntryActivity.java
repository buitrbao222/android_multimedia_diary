package com.example.multimedia_diary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreateEntryActivity extends AppCompatActivity {
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_VIDEO_CAPTURE = 4;

    private DiaryEntry entry = new DiaryEntry();
    private ArrayList<Bitmap> imageBitmaps = new ArrayList<>();
    private ArrayList<File> imageFiles = new ArrayList<>();
    private ImageListAdapter imageListAdapter;
    private GridView imageGridView;
    private Spinner weatherSpinner;
    private Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entry);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Generate entry unique id to use as prefix for images/videos
        entry.id = UUID.randomUUID().toString().replace("-", "");

        // Initialize image grid view
        imageGridView = findViewById(R.id.image_grid_view);
        imageListAdapter = new ImageListAdapter(this, R.layout.image_list_item, imageBitmaps);
        imageGridView.setAdapter(imageListAdapter);

        // Initialize weather spinner
        int[] weatherUnicodes = {0x2601, 0x26C5, 0x26C8, 0x1F324, 0x1F325, 0x1F326, 0x1F327, 0x1F328, 0x1F329, 0x1F32A, 0x1F32B};
        ArrayList<String> weatherEmojis = new ArrayList<>();
        for (int unicode : weatherUnicodes) {
            weatherEmojis.add(new String(Character.toChars(unicode)));
        }
        ArrayAdapter<String> weatherListAdapter = new ArrayAdapter<>(this, R.layout.weather_item, R.id.emoji, weatherEmojis);
        weatherSpinner = findViewById(R.id.weather_spinner);
        weatherSpinner.setAdapter(weatherListAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.create_entry_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_photo:
                try {
                    startTakePhotoActivity();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_add_video:
                try {
                    startTakeVideoActivity();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_save_location:
                saveLocation();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void saveLocation() {
        // Request location permission if not granted yet
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Request GPS if not enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(this)
                    .setTitle("GPS is not enabled!")
                    .setMessage("Do you want to turn on GPS?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.cancel();
                    })
                    .create()
                    .show();
            return;
        }

        Toast loadingToast = Toast.makeText(this, "Getting location...", Toast.LENGTH_LONG);
        loadingToast.show();

        // Get current location
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Set latitude and longitude to entry after location changed
                entry.latitude = location.getLatitude();
                entry.longitude = location.getLongitude();
                loadingToast.cancel();
                Toast.makeText(getApplicationContext(), "Saved location", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        }, null);
    }

    // Start take photo activity
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startTakePhotoActivity() throws IOException {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create photo file
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String prefix = entry.id + "_";
        File imageFile = File.createTempFile(prefix, ".jpg", storageDir);

        // Save file to array
        imageFiles.add(imageFile);

        // Start camera activity with file URI
        Uri imageUri = FileProvider.getUriForFile(this,
                "com.example.android.fileprovider",
                imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void startTakeVideoActivity() throws IOException {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // Create video file
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        String prefix = entry.id + "_";
        File videoFile = File.createTempFile(prefix, ".mp4", storageDir);

        // Start camera activity with uri
        videoUri = FileProvider.getUriForFile(this,
                "com.example.android.fileprovider",
                videoFile);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
    }

    // On take photo activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get bitmap from image
            Bitmap bitmap = BitmapFactory.decodeFile(imageFiles.get(imageFiles.size() - 1).getAbsolutePath());

            // Add image to entry
            imageBitmaps.add(bitmap);

            // Show image grid view
            imageGridView.setVisibility(View.VISIBLE);

            // Update image grid view
            imageListAdapter.notifyDataSetChanged();
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            LinearLayout videoLayoutView = findViewById(R.id.video_view_layout);
            videoLayoutView.setVisibility(View.VISIBLE);

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoLayoutView);

            VideoView videoView = findViewById(R.id.video_view);
            videoView.setVideoURI(videoUri);
            videoView.setMediaController(mediaController);
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Save entry to device
    public void onSaveClick(View view) throws IOException {
        EditText contentEditText = findViewById(R.id.input_content);
        entry.content = contentEditText.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        entry.createdAt = sdf.format(new Date());

        entry.weatherEmoji = weatherSpinner.getSelectedItem().toString();

        File file = new File(getFilesDir(), entry.createdAt);
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(entry);
        oos.close();
        fos.close();

        finish();
    }
}