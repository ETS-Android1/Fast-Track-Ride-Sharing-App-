package com.example.quassain.fasttrack;

import static android.os.Build.VERSION_CODES.S;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class SplashScreenActivity extends AppCompatActivity {


    private static int SPLASH_SCREEN_OUT=5000;

    Animation top,bottom,mid;
    View first,second,third,fourth,fifth,six;
    ImageView logo;
    TextView name;
    private static int timer=5000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        top= AnimationUtils.loadAnimation(this,R.anim.top_animation);
        mid= AnimationUtils.loadAnimation(this,R.anim.middle_animation);
        bottom= AnimationUtils.loadAnimation(this,R.anim.bottom_animation);


        first = findViewById(R.id.firstline);
        second = findViewById(R.id.secondline);
        third = findViewById(R.id.thirdline);
        fourth = findViewById(R.id.fourthline);
        fifth = findViewById(R.id.fifthline);
        six = findViewById(R.id.sixthline);
        logo = findViewById(R.id.mylogo);
        name = findViewById(R.id.fasttrack);


        //set animations

        first.setAnimation(top);
        second.setAnimation(top);
        third.setAnimation(top);
        fourth.setAnimation(top);
        fifth.setAnimation(top);
        six.setAnimation(top);

        logo.setAnimation(mid);

        name.setAnimation(bottom);
        //////////////////////////////

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreenActivity.this,WelcomeActivity.class);
                startActivity(i);
                finish();
            }
        },timer);


    }



}