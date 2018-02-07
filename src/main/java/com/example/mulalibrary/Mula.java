package com.example.mulalibrary;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Mula extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mula);
    }

    public Mula() {

    }


    public void configMula(Context context) {
        Intent intent = new Intent(context, Main.class);
        context.startActivity(intent);

    }
}
