package com.example.multimedia_diary;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ImageListAdapter extends BaseAdapter {
    private int resourceLayout;
    private final ArrayList<Bitmap> bitmaps;
    private final Context context;

    public ImageListAdapter(Context context, int resourceLayout, ArrayList<Bitmap> bitmaps) {
        this.context = context;
        this.resourceLayout = resourceLayout;
        this.bitmaps = bitmaps;
    }

    @Override
    public int getCount() {
        return bitmaps.size();
    }

    @Override
    public Bitmap getItem(int position) {
        return bitmaps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(resourceLayout, parent, false);
        }

        Bitmap bitmap = bitmaps.get(position);

        ImageView imageView = listItem.findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);

        return listItem;
    }
}
