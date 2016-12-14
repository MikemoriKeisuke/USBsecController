package com.example.s20143037.usbseccontroller;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class ConnectionActivity extends AppCompatActivity implements TextWatcher {

    private EditText editText; // 変更を検知するエディットボックス
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        editText = (EditText)findViewById(R.id.enterPinCode);
        editText.addTextChangedListener(this);

        textView = (TextView)findViewById(R.id.textView4);

    }

    public void intentConnComp(View v) {
        finish();
        Intent intent = new Intent(getApplication(), ConnectionCompActivity.class);
        startActivity(intent);
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
            textView.setFocusableInTouchMode(true);
            textView.requestFocus(View.FOCUS_UP);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
