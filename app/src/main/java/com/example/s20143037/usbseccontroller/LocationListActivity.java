package com.example.s20143037.usbseccontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        HashMap<String,String[]> locationMap=new HashMap<>();
        final ListView listView=(ListView) findViewById(R.id.location_list);
        ArrayList<String> macList=MyService.allMacAddress();
        String[] tempArray;
        int i=0;
        tempArray = macList.toArray(new String[0] );
        for(String temp:tempArray){
            ArrayList<String> tempList=MyService.PositionRead(temp);
            if(tempList.size()!=0) {
                tempArray[i] = temp+":"+tempList.get(tempList.size() - 1);
            }
            i++;
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,tempArray);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clicktext=(String)listView.getItemAtPosition(position);
                Intent intent=new Intent(getApplication(),SearchMapActivity.class);
                String macAddress=getMacAddress(clicktext);
                intent.putExtra("macAddress",macAddress);
                startActivity(intent);
            }
        });
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
    public String getMacAddress(String title){
        String macAddress=null;
        if(title.length()>17){
            macAddress=title.substring(0,17);
        }
        return macAddress;
    }
}
