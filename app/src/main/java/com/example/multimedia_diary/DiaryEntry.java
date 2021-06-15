package com.example.multimedia_diary;

import android.graphics.Bitmap;
import android.location.Location;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DiaryEntry implements Serializable {
    public String id, content, createdAt, weatherEmoji;
    public double latitude, longitude;

    public DiaryEntry() {

    }

    @Override
    public String toString() {
        return this.createdAt;
    }
}
