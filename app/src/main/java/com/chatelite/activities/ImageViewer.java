package com.chatelite.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.chatelite.R;
import com.squareup.picasso.Picasso;

public class ImageViewer extends AppCompatActivity {
    private ImageView imageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);
        imageView = findViewById(R.id.image_viewer);
        imageUrl = getIntent().getStringExtra("url");
        Picasso.get().load(imageUrl).into(imageView);
    }
}
