/* ブルートゥース通信　＆　現在地検索 */

package com.example.s20143037.usbseccontroller;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MyService extends Service {
    private final static int SDKVER_LOLLIPOP = 21;
    private final static int MESSAGE_NEW_RECEIVEDNUM = 0;
    private final static int MESSAGE_NEW_SENDNUM = 1;
    private final static int REQUEST_ENABLE_BT = 123456;
    static BluetoothManager mBleManager;
    private BluetoothAdapter mBleAdapter;
    private boolean mIsBluetoothEnable = false;
    private BluetoothLeScanner mBleScanner;
    static BluetoothGatt mBleGatt;
    Service main;

    static HashMap<String, BluetoothGatt> gattMap = new HashMap<>();
    private String mStrSendNum = "";
    static HashMap<String, String> deviceHash = new HashMap<>();
    public Parcel parcel;
    Notification.Action[] a;
    static HashMap<String, ArrayList<Location>> disconnList = new HashMap<>();
    static HashMap<String, ScanResult> resultList = new HashMap<>();
    //スキャンして　表示部とデータ部　にハッシュマップに追加
    ScanCallback ble = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // スキャン中に見つかったデバイスに接続を試みる.第三引数には接続後に呼ばれるBluetoothGattCallbackを指定する.

            BluetoothDevice b = result.getDevice();
            b.connectGatt(getApplicationContext(), true, mGattCallback);
            resultList.put(b.getAddress(), result);
            //
        }


        @Override
        public void onScanFailed(int intErrorCode) {
            super.onScanFailed(intErrorCode);
        }
    };
    //ブルートゥース接続後gatt追加
    private final BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            // スキャン中に見つかったデバイスに接続を試みる.第三引数には接続後に呼ばれるBluetoothGattCallbackを指定する.

            mBleGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
        }
    };
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // 接続状況が変化したら実行.
            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                // 接続に成功したらサービスを検索する.
//                Log.d(this.toString(), "終わりました")
                BluetoothDevice b = gatt.getDevice();
                deviceHash.put(b.getAddress(), b.getName());
                gattMap.put(gatt.getDevice().getAddress(), gatt);

//                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 接続が切れたらGATTを空にする.
                if (mBleGatt != null) {
                    ArrayList<Location> tempList = disconnList.get(mBleGatt.getDevice().getAddress());
                    if (tempList == null) {
                        tempList = new ArrayList<>();
                    }
                    //location ゲット＆Location 追加
                    //MapControllerの取得
                    //LocationManagerの取得
                    LocationManager locationManager = (LocationManager) main.getSystemService(Context.LOCATION_SERVICE);
                    //GPSから現在地の情報を取得
                    if (ActivityCompat.checkSelfPermission(main, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(main, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Location myLocate = locationManager.getLastKnownLocation("gps");

                    tempList.add(myLocate);
                    disconnList.put(mBleGatt.getDevice().getAddress(), tempList);
                    deviceHash.remove(mBleGatt.getDevice().getAddress());
                    gattMap.remove(mBleGatt.getDevice().getAddress());
                    mBleGatt.close();
                    mBleGatt = null;
                }
                mIsBluetoothEnable = false;
            }
        }
    };
    private Handler mBleHandler = new Handler() {
        public void handleMessage(Message msg) {
            // UIスレッドで実行する処理.
            switch (msg.what) {
                case MESSAGE_NEW_RECEIVEDNUM:
                    /*mTxtReceivedNum.setText(mStrReceivedNum);*/
                    break;
                case MESSAGE_NEW_SENDNUM:
/*
                    mTxtSendNum.setText(mStrSendNum);
*/
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        main=this;
        Intent intent=new Intent(this,CardListActivity.class);
        Intent[] i=new Intent[1];
        i[0]=intent;
        PendingIntent pi= PendingIntent.getActivities(this,0,i,0);

        Notification notification=new Notification.Builder(this)
                .setContentTitle("できた")
                .setContentText("うんこ")
                .setContentIntent(pi)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLocalOnly(true)
                .build();
        NotificationManager nm=(NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0,notification);

        //startForeground(1,notification);

        mIsBluetoothEnable = false;
        // Writeリクエストで送信する値、Notificationで受け取った値をセットするTextView.
        //mTxtReceivedNum = (TextView) findViewById(R.id.received_num);
        //mTxtSendNum = (TextView) findViewById(R.id.send_num);

        // Bluetoothの使用準備.
        mBleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBleAdapter = mBleManager.getAdapter();

        // BLEが使用可能ならスキャン開始.
        this.scanNewDevice();
        mBleScanner.startScan(ble);
    }

    private void scanNewDevice() {
        // OS ver.5.0以上ならBluetoothLeScannerを使用する.
        if (Build.VERSION.SDK_INT >= SDKVER_LOLLIPOP) {
            this.startScanByBleScanner();
        } else {
            // デバイスの検出.
            mBleAdapter.startLeScan(mScanCallback);
        }
    }

    @TargetApi(SDKVER_LOLLIPOP)
    private void startScanByBleScanner() {
        mBleScanner = mBleAdapter.getBluetoothLeScanner();
    }


    @Override
    public void onDestroy() {
        // 画面遷移時は通信を切断する.
        mIsBluetoothEnable = false;
        if (mBleGatt != null) {
            mBleGatt.close();
            mBleGatt = null;
        }
        super.onDestroy();
    }
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public static void readCharacteristic(String address , String sid, String cid) {
        mBleGatt=gattMap.get(address);
        BluetoothGattService s=mBleGatt.getService(UUID.fromString(sid));
        BluetoothGattCharacteristic read = s.getCharacteristic(UUID.fromString(cid));
        mBleGatt.readCharacteristic(read);
    }

    @Nullable
    public static BluetoothGattCharacteristic getCharacteristic(String address, String sid, String cid) {
        BluetoothGatt gatt=gattMap.get(address);
        if(gatt==null){
            return null;
        }
        BluetoothGattService s=gatt.getService(UUID.fromString(sid));
        if (s == null) {
            return null;
        }
        s.getCharacteristic(UUID.fromString(cid));
        BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString(cid));
        if (c == null) {
            return null;
        }
        BluetoothGattCharacteristic asda=c;
        return c;
    }
    public static ArrayList<Location> getLocationList(String macAddress){
        return disconnList.get(macAddress);
    }
}
