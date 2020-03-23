package com.chatelite.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

import com.chatelite.R;


public class SplashScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        new CountDownTimer(2000, 2000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                startActivity(new Intent(SplashScreen.this, Main.class));

                finish();
            }
        }.start();

    }

}
