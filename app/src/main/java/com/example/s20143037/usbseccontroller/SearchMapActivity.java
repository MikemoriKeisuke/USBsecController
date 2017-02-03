package com.example.s20143037.usbseccontroller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

    private double latitude = 43.061105;
    private double longitude = 141.356432;
    private String caption = "2016/12/12";
    Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        String macAddress = getIntent().getStringExtra("macAddress");
        ArrayList<String> position=MyService.PositionRead(macAddress);
        if (position.size()<1) {
            Toast.makeText(this, "使用履歴がありません", Toast.LENGTH_LONG).show();
            LatLng location = new LatLng(latitude, longitude);

            //        if (locationList != null) {
            //            for (Location tempLoc : locationList) {
            //                location = new LatLng(tempLoc.getLatitude(), tempLoc.getLongitude());
            //                mMap.addMarker(new MarkerOptions().position(location).title(new Date(tempLoc.getTime()).toString()).snippet(caption));
            //            }
            //        }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(location, 14);
            mMap.moveCamera(cUpdate);
            if (Build.VERSION.SDK_INT >= 23) {
                checkPermission();
            }
        }
        ArrayList<LatLng> locationList=new ArrayList<>();
        for(String temp:position) {
            String work[] =temp.split(",", 0);
                caption = work[0];
                latitude = Double.parseDouble(work[1]);

                longitude = Double.parseDouble(work[2]);
                LatLng location = new LatLng(latitude, longitude);
            locationList.add(location);

                //        if (locationList != null) {
                //            for (Location tempLoc : locationList) {
                //                location = new LatLng(tempLoc.getLatitude(), tempLoc.getLongitude());
                //                mMap.addMarker(new MarkerOptions().position(location).title(new Date(tempLoc.getTime()).toString()).snippet(caption));
                //            }
                //        }
            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(caption));
            mMarker.showInfoWindow();

            CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(location, 14);
            mMap.moveCamera(cUpdate);
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermission();
                }
        }
        if(locationList.size()>=3){
            //予測地点表示
            int i=1;
            //円方程式 x^2+y^2+xl+ym+n=0
            LatLng loc=locationList.get(locationList.size()-i);
            //一つ目の位置ログ-二つ目のログ
            //(x^1-x2^2+y1^2-y2^2)

            double x1=loc.latitude;
            double y1=loc.longitude;
            double x2=loc.latitude;
            double y2=loc.longitude;
//            x1=2;
//            y1=1;
//            x2=1;
//            y2=2;
            while(x1==x2&&y1==y2&&locationList.size()>=i+1){
                i++;
                loc=locationList.get(locationList.size()-i);
                x2=loc.latitude;
                y2=loc.longitude;
            }

            double x3=loc.latitude;
            double y3=loc.longitude;
//            x3=0;
//            y3=1;
            while((x1==x3&&y1== y3|| x2==x3&&y2== y3)&&locationList.size()>=i+1){
                i++;
                loc=locationList.get(locationList.size()-i);

                x3=loc.latitude;
                y3=loc.longitude;
            }

            double resultLat=((y1-y3)*(y1*y1 -y2*y2 +x1*x1 -x2*x2) -(y1-y2)*(y1*y1 -y3*y3 +x1*x1 -x3*x3)) / (2*(y1-y3)*(x1-x2)-2*(y1-y2)*(x1-x3));
            double resultLng=((x1-x3)*(x1*x1 -x2*x2 +y1*y1 -y2*y2) -(x1-x2)*(x1*x1 -x3*x3 +y1*y1 -y3*y3)) /(2*(x1-x3)*(y1-y2)-2*(x1-x2)*(y1-y3));
            if(locationList.size()>=i+1) {
                BitmapDescriptor mIcon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_pin2);
                LatLng location = new LatLng(resultLat, resultLng);
                double r=(x1-resultLat)*(x1-resultLat)+(y1-resultLng)*(y1-resultLng);
                r=r/2;
                r=r*1300*1000*1000;
                float[] results = new float[3];
                Location.distanceBetween(
                        x1,
                        y1,
                        resultLat,
                        resultLng,
                        results);

                float zoom=(float)Math.log10(r/10);
                if(zoom<0){
                    zoom=0;
                }
                zoom=18-zoom;
                mMap.addMarker(new MarkerOptions().position(location).icon(mIcon));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                CircleOptions circleOptions=new CircleOptions()
                        .center(location)
                        .strokeColor(Color.TRANSPARENT)
                        .fillColor(R.color.icons)
                        .radius(results[0]*1.1);
                mMap.addCircle(circleOptions);
                CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(location, zoom);
                mMap.moveCamera(cUpdate);
            }else{
                Toast.makeText(this,"データが少なく見つけられませんでした",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,"データが少なく見つけられませんでした",Toast.LENGTH_SHORT).show();
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


}
