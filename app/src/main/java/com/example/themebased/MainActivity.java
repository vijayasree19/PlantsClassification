package com.example.themebased;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public  void onRestart()
    {
        super.onRestart();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
}
