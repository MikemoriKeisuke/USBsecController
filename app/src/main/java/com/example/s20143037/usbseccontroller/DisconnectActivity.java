package com.example.s20143037.usbseccontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DisconnectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disconnect);
    }

    public void intentDisConnComp(View v) {
        finish();
        Intent intent = new Intent(getApplication(), DisconnectCompActivity.class);
        startActivity(intent);
    }
}
