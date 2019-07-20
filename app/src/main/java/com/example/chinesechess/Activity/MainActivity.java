package com.example.chinesechess.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chinesechess.R;

public class MainActivity extends AppCompatActivity {
    Button start, end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.buttonstart);
        end = findViewById(R.id.buttonend);
        start.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CoTuongActivity.class);
            startActivity(intent);
            finish();
        });

        end.setOnClickListener(view -> finish());
    }
}


