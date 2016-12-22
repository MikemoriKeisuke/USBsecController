package com.example.s20143037.usbseccontroller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

public class SearchMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    ArrayList<Location> locationList;
    private double latitude = 43.055934;
    private double longitude = 141.3775153;
    private String caption = "2016/12/12";
    String work[];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_map);
        String macAddress =getIntent().getStringExtra("macAddress");
        locationList=MyService.getLocationList(macAddress);

        work = PositionRead(macAddress).split(",", 0);
        if (work!=null) {
            caption  = work[1];
            latitude = Double.parseDouble(work[2]);
            longitude= Double.parseDouble(work[3]);
        } else {
            Toast.makeText(this,"まだ使用履歴がありません。" , Toast.LENGTH_LONG).show();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        if (work!=null) {
            LatLng location = new LatLng(latitude, longitude);
            if (locationList != null) {
                for (Location tempLoc : locationList) {
                    location = new LatLng(tempLoc.getLatitude(), tempLoc.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(location).title(new Date(tempLoc.getTime()).toString()).snippet(caption));

                }
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(location, 14);
            mMap.moveCamera(cUpdate);
            if (Build.VERSION.SDK_INT >= 23) {
                checkPermission();
            }
        }
    }


    // 位置情報許可の再確認
    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
//        }
//        else{
//            mMap.setMyLocationEnabled(true);
//        }
    }

    //位置情報読み込み
    //macアドレスから取得
    public String PositionRead (String mac) {
        InputStream in;
        String lineBuffer;
        String str="";

        mac = mac.replaceAll(":","");

        try {
            in = openFileInput(mac + ".txt");

            BufferedReader reader= new BufferedReader(new InputStreamReader(in,"UTF-8"));
            while( (lineBuffer = reader.readLine()) != null ){
                str += lineBuffer + "\n";
            }
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        return str;
    }


}
