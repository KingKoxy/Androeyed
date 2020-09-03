package com.example.textrecognitionapp;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

public class StartActivity extends AppCompatActivity {

    MotionLayout motionLayout;
    ImageView ivLogo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ivLogo = findViewById(R.id.ivLogo);
        AnimatedVectorDrawable logo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            logo = (AnimatedVectorDrawable) ivLogo.getDrawable();
            logo.start();
        }


        motionLayout = findViewById(R.id.motionLayout);
        motionLayout.transitionToEnd();

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"};
        final int REQUEST_CODE = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, REQUEST_CODE);
        }
    }
}
