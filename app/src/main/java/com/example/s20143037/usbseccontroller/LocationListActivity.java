package com.example.s20143037.usbseccontroller;

import android.location.Location;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.example.s20143037.usbseccontroller.MyService.disconnList;


public class LocationListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        HashMap<String,String[]> locationMap=new HashMap<>();
        ListView listView=(ListView) findViewById(R.id.location_list);
        ArrayList<String> macList=MyService.allMacAddress();
        String[] tempArray;
        tempArray = macList.toArray(new String[0] );
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,tempArray);
        listView.setAdapter(adapter);
    }
    public ArrayList<String[]> PositionRead(String mac) {
        InputStream in;
        String lineBuffer;
        ArrayList<String[]> str =new ArrayList<>();
        int i=0;

        try {
            in = openFileInput(mac + ".txt");

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            while ((lineBuffer = reader.readLine()) != null) {
                str.add(lineBuffer.split(","));
            }
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        return str;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
