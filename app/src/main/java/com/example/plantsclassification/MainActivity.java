package com.example.plantsclassification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private Handler handler= new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                  doStuff();
            }
        }, 5000);
    }

        void doStuff()
        {
            Intent intent = new Intent(MainActivity.this,Login.class);
            startActivity(intent);
        }

    }


