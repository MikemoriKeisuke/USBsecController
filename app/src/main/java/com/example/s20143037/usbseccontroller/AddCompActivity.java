package com.example.s20143037.usbseccontroller;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AddCompActivity extends AppCompatActivity implements Runnable {
    ProgressDialog progressDialog;
    Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comp);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("タイトル");
        progressDialog.setMessage("メッセージ");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            thread.sleep(5000);
        } catch (InterruptedException e) { }
        progressDialog.dismiss();
    }
}
