package com.example.solo.autoweixin.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.fragment.Fragment1;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (Fragment1.fragment1 != null) {
            Fragment1.fragment1.CustomTimeToast(false);
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
