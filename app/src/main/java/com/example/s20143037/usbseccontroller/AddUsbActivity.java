package com.example.s20143037.usbseccontroller;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class AddUsbActivity extends AppCompatActivity implements TextWatcher {

    private EditText editText; // 変更を検知するエディットボックス
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_usb);

        editText = (EditText)findViewById(R.id.editPinCode);
        editText.addTextChangedListener(this);

    }


    public void intentAddComp(View v) {
        finish();
        String scomm=editText.getText().toString();
        byte[] bcomm=new byte[6];
        for(int i=0;i<scomm.length();i++){
            int temp=Integer.parseInt(scomm.substring(i,i+1));
            bcomm[i]=(byte)temp;
        }
        Intent intent = new Intent(getApplication(), AddCompActivity.class);
        startActivity(intent);
        intent = getIntent();
        String mac=intent.getStringExtra("macAddress");
        MyService.pinMap.put(mac,bcomm);
        MyService.writeCharacteristic(mac,"0000a001-0000-1000-8000-00805f9b34fb","0000a011-0000-1000-8000-00805f9b34fb",bcomm);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MyService.writeCharacteristic(mac,"0000a002-0000-1000-8000-00805f9b34fb","0000a021-0000-1000-8000-00805f9b34fb",bcomm);


    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String inputStr= editable.toString();

        if(inputStr.length() >=5){
            editText.setFocusableInTouchMode(true);
            editText.requestFocus(View.FOCUS_UP);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
