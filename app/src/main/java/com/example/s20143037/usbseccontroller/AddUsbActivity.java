package com.example.s20143037.usbseccontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AddUsbActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_usb);
    }

    public void intentAddComp(View v) {
        Intent intent = new Intent(getApplication(), AddCompActivity.class);
        startActivity(intent);
    }
}
