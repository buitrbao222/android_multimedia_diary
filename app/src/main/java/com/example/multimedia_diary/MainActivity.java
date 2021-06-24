package com.example.multimedia_diary;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Multimedia Diary");

        // Initialize image loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        ListView listView = findViewById(R.id.listview);
        ArrayList<DiaryEntry> entries = new ArrayList<>();

        File dir = getFilesDir();

        File[] list = dir.listFiles();

        for (File f : list) {
            if (f.isFile()) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    DiaryEntry entry = (DiaryEntry) ois.readObject();
                    entries.add(entry);
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        entries.sort((o1, o2) -> o2.createdAt.compareTo(o1.createdAt));

        ArrayAdapter<DiaryEntry> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entries);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, EntryDetailsActivity.class);
            intent.putExtra("entry", (Serializable) parent.getItemAtPosition(position));
            startActivity(intent);
        });

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new_entry) {
            Intent intent = new Intent(this, CreateEntryActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }
}