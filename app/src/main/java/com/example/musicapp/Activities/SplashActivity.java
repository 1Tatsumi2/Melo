package com.example.musicapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicapp.R;

public class SplashActivity extends AppCompatActivity {
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler =new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this,RegisterActivity.class);
            startActivity(intent);
            finish();
        },3000);


    }
}