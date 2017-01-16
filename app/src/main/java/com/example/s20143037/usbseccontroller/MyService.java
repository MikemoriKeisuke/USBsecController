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
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mLocationClient;
    LocationManager locationManager;
    HashMap<String,byte[]> acceptPinMap=new HashMap<>();
    private final static int SDKVER_LOLLIPOP = 21;
    private final static int MESSAGE_NEW_RECEIVEDNUM = 0;
    private final static int MESSAGE_NEW_SENDNUM = 1;
    static BluetoothManager mBleManager;
    private BluetoothAdapter mBleAdapter;
    private boolean mIsBluetoothEnable = false;
    private BluetoothLeScanner mBleScanner;
    static BluetoothGatt mBleGatt;
    static HashMap<String,Boolean> addAbleMap=new HashMap<>();
    static HashMap<String,byte[]> pinMap=new HashMap<>();
    Service main;
    static Location location;
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
            mBleGatt = b.connectGatt(getApplicationContext(), true, mGattCallback);

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

                gatt.discoverServices();
                BluetoothDevice b = gatt.getDevice();
                deviceHash.put(b.getAddress(), b.getName());
                gattMap.put(gatt.getDevice().getAddress(), gatt);
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

                    if(acceptPinMap.containsKey(gatt.getDevice().getAddress())) {
                        Intent intent = new Intent(main, CardListActivity.class);
                        Intent[] i = new Intent[1];
                        i[0] = intent;
                        PendingIntent pi = PendingIntent.getActivities(main, 0, i, 0);

                        Notification notification = new Notification.Builder(main)
                                .setContentTitle("切れちゃったよ")
                                .setContentText("どうにかしてよ")
                                .setContentIntent(pi)
                                .setSmallIcon(R.drawable.ic_sec)
                                .setLocalOnly(true)
                                .build();

                        //削除できないように設定
                        //        notification.flags |= Notification.FLAG_ONGOING_EVENT;

                        NotificationManager nm = (NotificationManager) main.getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.notify(0, notification);

                    }
                    tempList.add(getLastLocation());
                    disconnList.put(mBleGatt.getDevice().getAddress(), tempList);
                    String macaddress = mBleGatt.getDevice().getAddress();
                    PositionSave(macaddress, getLastLocation().getLatitude(), getLastLocation().getLongitude());


                    deviceHash.remove(macaddress);
                    gattMap.remove(macaddress);
                    mBleGatt.close();
                    mBleGatt = null;
                }
                mIsBluetoothEnable = false;
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic,int a){
            if(UUID.fromString("0000a021-0000-1000-8000-00805f9b34fb").equals(characteristic.getUuid())){

            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic,int a){
            byte[] value=characteristic.getValue();
            if(characteristic.getUuid().equals(UUID.fromString("0000a012-0000-1000-8000-00805f9b34fb"))){
                byte temp=value[0];
                if(temp==(byte)0){
                    addAbleMap.put(gatt.getDevice().getAddress(),true);
                }else{
                    addAbleMap.put(gatt.getDevice().getAddress(),false);
                }
            }
            if(UUID.fromString("0000a041-0000-1000-8000-00805f9b34fb").equals(characteristic.getUuid())){
                byte temp=value[0];
                if(temp==(byte)1){
                    String macAddress=gatt.getDevice().getAddress();
                    acceptPinMap.put(macAddress,pinMap.get(macAddress));
                }
            }}
        @Override

        public void onServicesDiscovered(BluetoothGatt gatt,int status){
            if(gatt.getService(UUID.fromString("0000a001-0000-1000-8000-00805f9b34fb"))!=null){
                readCharacteristic(gatt.getDevice().getAddress(), "0000a001-0000-1000-8000-00805f9b34fb", "0000a012-0000-1000-8000-00805f9b34fb");
            }
            if(gatt.getService(UUID.fromString("0000a002-0000-1000-8000-00805f9b34fb"))!=null){
                if(acceptPinMap.containsKey(gatt.getDevice().getAddress())) {
                    writeCharacteristic(gatt.getDevice().getAddress(),"0000a002-0000-1000-8000-00805f9b34fb","0000a021-0000-1000-8000-00805f9b34fb",acceptPinMap.get(gatt.getDevice().getAddress()));
                }
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
        main = this;
        locationManager = (LocationManager) main.getSystemService(Context.LOCATION_SERVICE);
        Intent intent = new Intent(this, CardListActivity.class);
        Intent[] i = new Intent[1];
        i[0] = intent;
        PendingIntent pi = PendingIntent.getActivities(this, 0, i, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("できた")
                .setContentText("うんこ")
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_sec)
                .setLocalOnly(true)
                .build();

        //削除できないように設定
//        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, notification);

        //startForeground(1,notification);

        mIsBluetoothEnable = false;
//         Writeリクエストで送信する値、Notificationで受け取った値をセットするTextView.
//        mTxtReceivedNum = (TextView) findViewById(R.id.received_num);
//        mTxtSendNum = (TextView) findViewById(R.id.send_num);

        // Bluetoothの使用準備.
        mBleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBleAdapter = mBleManager.getAdapter();
        android.os.ParcelUuid parcelUuid = new android.os.ParcelUuid(UUID.fromString("0000a001-0000-1000-8000-00805f9b34fb"));
        ScanFilter scanFilter =
                new ScanFilter.Builder()
                        .setServiceUuid(parcelUuid)
                        .build();
        ArrayList scanFilterList = new ArrayList();
        scanFilterList.add(scanFilter);

        ScanSettings scanSettings =
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .build();
        // BLEが使用可能ならスキャン開始.
        this.scanNewDevice();
        mBleScanner.startScan(scanFilterList, scanSettings, ble);
        //gps起動
        Context context = this;
        mLocationClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationClient.connect();

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

    public static void readCharacteristic(String address, String sid, String cid) {
        BluetoothGatt gatt = gattMap.get(address);
        BluetoothGattService s = gatt.getService(UUID.fromString(sid));
        BluetoothGattCharacteristic read = s.getCharacteristic(UUID.fromString(cid));
        gatt.readCharacteristic(read);
    }

    @Nullable
    public static BluetoothGattCharacteristic getCharacteristic(String address, String sid, String cid) {
        BluetoothGatt gatt = gattMap.get(address);
        if (gatt == null) {
            return null;
        }
        BluetoothGattService s = gatt.getService(UUID.fromString(sid));
        if (s == null) {
            return null;
        }
        s.getCharacteristic(UUID.fromString(cid));
        BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString(cid));
        if (c == null) {
            return null;
        }
        BluetoothGattCharacteristic asda = c;
        return c;

    }

    public static ArrayList<Location> getLocationList(String macAddress) {
        return disconnList.get(macAddress);
    }

    public static void writeCharacteristic(String address, String sid, String cid, byte[] comment) {
        BluetoothGatt gatt = gattMap.get(address);
        BluetoothGattCharacteristic write = getCharacteristic(
                sid, cid, gatt);
        byte[] message = comment;
        write.setValue(message);
        gatt.writeCharacteristic(write);
    }

    public static BluetoothGattCharacteristic getCharacteristic(String sid, String cid, BluetoothGatt gatt) {
        BluetoothGattService s = gatt.getService(UUID.fromString(sid));
        if (s == null) {
            Log.w(TAG, "Service NoT found :" + sid);
            return null;
        }
        BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString(cid));
        if (c == null) {
            Log.w(TAG, "Characteristic NOT found :" + cid);
            return null;
        }
        return c;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public Location getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        return LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
    }

    //位置情報保存
    //macアドレスと緯度経度を渡すと最新の分のみ保存するよ
    //追加するよの方を使えば追加保存になるよ
    public void PositionSave(String mac, double latitude, double longitude) {
        OutputStream out;

        Calendar date = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日");

        try {
            out = openFileOutput((mac + ".txt"), MODE_PRIVATE | MODE_APPEND);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));

            //上書きするよ
            writer.write(sdf.format(date.getTime()) + "," + latitude + "," + longitude + "\n");

            //追加するよ
//            writer.append(sdf.format(date.getTime()) + "," + latitude + "," + longitude + "\n");

            writer.close();


        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }
}
