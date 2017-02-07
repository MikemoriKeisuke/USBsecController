package com.example.s20143037.usbseccontroller;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.analytics.Analytics;
import com.microsoft.azure.mobile.crashes.Crashes;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class CardListActivity extends AppCompatActivity  {
    private RecyclerView mRecyclerView;
    private UsbAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LocationManager nlLocationManager;
    final int REQUEST_ENABLE_BLUETOOTH=2;
    boolean destory = false;
    Context context;
    static Activity main;
    Thread running;
    BluetoothAdapter ba;
    final HashMap<String,String> x= new HashMap();
    private static final int MENU_ID_A=0;
    private static final int MENU_ID_B=1;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,MENU_ID_A,Menu.NONE,"表示されないUSBsecの探索");
        menu.add(Menu.NONE,MENU_ID_B,menu.NONE,"接続履歴の削除");

        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case MENU_ID_A:
                Intent intent=new Intent(getApplicationContext(),LocationListActivity.class);
                startActivity(intent);
                break;
            case MENU_ID_B:
                new AlertDialog.Builder(main).setTitle("すべての接続履歴を削除します。\nよろしいですか？")
                        .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<String> macList = MyService.allMacAddress();
                                InputStream in;
                                String lineBuffer;
                                for (String mac:macList) {

                                    deleteFile(mac + ".txt");

                                }
                            }
                        })
                .setNegativeButton("いいえ",null)
                .show();
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: { //ActivityCompat#requestPermissions()の第2引数で指定した値
                ba=BluetoothAdapter.getDefaultAdapter();
                if (grantResults.length > 0 && ba.isEnabled()) {

                    //許可された場合の処理
                }else{
                    //拒否された場合の処理
                    finish();
                }
                break;
            }
        }
    }
    protected void initialize(){
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        final BluetoothLeScanner bluetoothLeScanner = adapter.getBluetoothLeScanner();

        BluetoothAdapter ba=
                BluetoothAdapter.getDefaultAdapter();

        boolean btEnable = ba.isEnabled();
        if(btEnable == true){
            //BluetoothがONだった場合の処理
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 0);
        }else{
            //OFFだった場合、ONにすることを促すダイアログを表示する画面に遷移
            Intent btOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btOn,REQUEST_ENABLE_BLUETOOTH);
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 0);
        }
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPSが有効になっていません。\n有効化しますか？")
                    .setCancelable(false)

                    //GPS設定画面起動用ボタンとイベントの定義
                    .setPositiveButton("GPS設定起動",
                            new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id){
                                    Intent callGPSSettingIntent = new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(callGPSSettingIntent);
                                }
                            });
            //キャンセルボタン処理
            alertDialogBuilder.setNegativeButton("キャンセル",
                    new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id){
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            // 設定画面へ移動するかの問い合わせダイアログを表示
            alert.show();

        }

        // 6.0以降はコメントアウトした処理をしないと初回はパーミッションがOFFになっています。

    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecyclerView.setBackground(getDrawable(R.color.cardview_light_background));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileCenter.start(getApplication(), "{Your App Secret}", Analytics.class, Crashes.class);
        main = this;

        context=this;
        initialize();
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> listServiceInfo = am.getRunningServices(Integer.MAX_VALUE);
        boolean found = false;

        setContentView(R.layout.activity_card_list);
        nlLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        mRecyclerView.setBackground(getDrawable(R.color.cardview_light_background));
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        for (ActivityManager.RunningServiceInfo curr : listServiceInfo) {
            // ｸﾗｽ名を比較
            if (curr.service.getClassName().equals(MyService.class.getName())) {
                // 実行中のｻｰﾋﾞｽと一致
                Toast.makeText(this, "ｻｰﾋﾞｽ実行中", Toast.LENGTH_LONG).show();
                found = true;
            }else{
                final Intent intent=new Intent(this, MyService.class);
                startService(intent);
            }
        }

        running = new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> aa = new ArrayList<>();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.setAdapter(mAdapter);

                    }
                });
                final ArrayList<String> DataSet = new ArrayList<>();

                mAdapter=new UsbAdapter(context);

                while (true) {
                    if (destory) {
                        break;
                    }
                    if(mAdapter.wait) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        final HashMap<String, String> deviceHash = MyService.deviceHash;
                        final HashMap<String,String> deleteMap=new HashMap<>();
                        final ArrayList<String> deleteList=mAdapter.onDataList;
                        for(String temp:deleteList){
                            deleteMap.put(getMacAddress(temp),temp);
                        }
                        for (String key : deviceHash.keySet()) {
                            String dev = deviceHash.get(key);
                            if(deleteMap.containsKey(key)) {
                                deleteMap.remove(key);
                            }

                            String map=x.get(key);
                            if(map==null) {
                                x.put(key,dev);
                                if (dev == null) {
                                    mAdapter.addAdapter("null  :  " + key);
                                } else {
                                    mAdapter.addAdapter(dev + "  :  " + key);
                                }
                            }
                            if(MyService.mBleScanner!=null) {
                                MyService.sendAuth(key);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                MyService.readCharacteristic(key, "0000a001-0000-1000-8000-00805f9b34fb", "0000a012-0000-1000-8000-00805f9b34fb");
                            }
                        }
                        for(String temp :deleteMap.keySet()){
                            mAdapter.deleteAdapter(deleteMap.get(temp));
                            x.remove(temp);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mRecyclerView.setAdapter(mAdapter);
                                if(mAdapter.getItemCount()==0) {
                                    mRecyclerView.setBackground(getDrawable(R.drawable.during_search));
                                }else{
                                    mRecyclerView.setBackground(getDrawable(R.color.cardview_light_background));
                                }
                            }
                        });
                    }
                }
            }

        });

        running.start();

    }


    public void intentConn(View v) {
        TextView macView=(TextView) findViewById(R.id.UsbNameView);
        String mac=macView.getText().toString();
        String macAddress=getMacAddress(mac);
        Intent intent = new Intent(getApplication(), ConnectionActivity.class);
        intent.putExtra("macAddress",macAddress);
        startActivity(intent);
        overridePendingTransition ( R.anim.in_anim, R.anim.out_anim);
        Toast.makeText(this, String.valueOf(mac), Toast.LENGTH_SHORT).show();
    }

    public void intentSearchMap(View v) {
        Intent intent = new Intent(getApplication(), SearchMapActivity.class);
        TextView macView=(TextView) findViewById(R.id.UsbNameView);
        String mac=macView.getText().toString();
        String macAddress=(mac).substring(mac.length()-17);
        intent.putExtra("macAddress",macAddress);
        startActivity(intent);
        overridePendingTransition ( R.anim.in_anim, R.anim.out_anim);
    }

    public void intentAddUsb(View v) {
        Intent intent = new Intent(getApplication(), AddUsbActivity.class);
        startActivity(intent);
        overridePendingTransition ( R.anim.in_anim, R.anim.out_anim);
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        }
        if (!nlLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPSが有効になっていません。\n有効化しますか？")
                    .setCancelable(false)

                    .setPositiveButton("GPS設定起動",
                            new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id){
                                    Intent callGPSSettingIntent = new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(callGPSSettingIntent);
                                }
                            });
            alertDialogBuilder.setNegativeButton("キャンセル",
                    new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id){
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            new AlertDialog.Builder(this)
                    .setTitle("アプリケーションの終了")
                    .setMessage("アプリケーションを終了してよろしいですか？")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO 自動生成されたメソッド・スタブ
                            CardListActivity.this.destory=true;
                            CardListActivity.this.finish();
                            MyService.destory();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO 自動生成されたメソッド・スタブ
                        }
                    })
                    .show();

            return true;
        }
        return false;
    }

    static String getMacAddress(String title){
        String temp="";
        String mac=title;
        temp=(mac).substring(mac.length()-17);
        return temp;
    }

}