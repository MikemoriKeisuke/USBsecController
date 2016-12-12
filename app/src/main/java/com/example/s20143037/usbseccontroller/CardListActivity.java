package com.example.s20143037.usbseccontroller;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

public class CardListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView1);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // テストデータを作成
        final ArrayList<String> myDataSet = new ArrayList<String>() {
            {
                add("USBsec1");
                add("USBsec2");
                add("USBsec3");
            }
        };

        // アダプタを指定する
        mAdapter = new UsbAdapter(this, myDataSet);
        mRecyclerView.setAdapter(mAdapter);
        checkPermission();
    }

    public void intentConn(View v) {
        Intent intent = new Intent(getApplication(), ConnectionActivity.class);
        startActivity(intent);
    }

    public void intentSearchMap(View v) {
        Intent intent = new Intent(getApplication(), SearchMapActivity.class);
        startActivity(intent);
    }

    public void intentAddUsb(View v) {
        Intent intent = new Intent(getApplication(), AddUsbActivity.class);
        startActivity(intent);
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
        }
    }

}