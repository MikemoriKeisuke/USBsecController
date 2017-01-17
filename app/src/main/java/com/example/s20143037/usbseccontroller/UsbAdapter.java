package com.example.s20143037.usbseccontroller;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.support.v7.recyclerview.R.styleable.RecyclerView;
import static com.example.s20143037.usbseccontroller.CardListActivity.main;

public class UsbAdapter extends RecyclerView.Adapter<UsbAdapter.ViewHolder> {
    private LayoutInflater onLayoutInflater;
     ArrayList<String> onDataList;
    static ArrayList<Boolean>onBoolean ;
    static boolean wait=true;
    static Context contxt;

    int count=0;

    public UsbAdapter(Context context) {
        super();
        contxt=context;
        onLayoutInflater = LayoutInflater.from(context);
        onDataList = new ArrayList<>();
        onBoolean=new ArrayList<>();
        while(count<onDataList.size()){
            onBoolean.add(false);
            count++;
        }
    }

    // getViewのinfrateするところだけ取り出した感じ
    @Override
    public UsbAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = onLayoutInflater.inflate(R.layout.card_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }
    @Override
    public int getItemCount() {
        return onDataList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String data = (String) onDataList.get(position);
        Boolean boolean2=(boolean) onBoolean.get(position);
        if(boolean2==null){
            boolean2=false;
        }

        holder.mSwitch.setChecked(boolean2);
        holder.text.setText(data);
    }

    // ViewHolder内でwidgetを割り当てる
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        Switch mSwitch;
        Button conButton;

        public ViewHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.UsbNameView);
            mSwitch=(Switch) v.findViewById(R.id.switch1);
            conButton=(Button)v.findViewById(R.id.IntentConnButton);
            conButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String temp=text.getText().toString();
                    temp=CardListActivity.getMacAddress(temp);
                    String macAddress=CardListActivity.getMacAddress(temp);
                    try {
                        if (MyService.addAbleMap.get(macAddress)) {

                            MyService.readCharacteristic(macAddress, "0000a001-0000-1000-8000-00805f9b34fb", "0000a012-0000-1000-8000-00805f9b34fb");
                            Intent intent = new Intent(contxt, AddUsbActivity.class);
                            intent.putExtra("macAddress", macAddress);
                            main.startActivity(intent);
                            main.overridePendingTransition(R.anim.in_anim, R.anim.out_anim);
                            Toast.makeText(main, String.valueOf(macAddress), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(main,String.valueOf("すでにパスワードが登録されています。"),Toast.LENGTH_SHORT).show();
                        }
                    }catch (NullPointerException e){

                    }

                }
            });
            mSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bool=onBoolean.get(getAdapterPosition());
                    String mac=CardListActivity.getMacAddress(text.getText().toString());
                    if(bool){
                        int temp=1;
                        byte[] test=new byte[1];
                        test[0]=(byte)temp;
                        MyService.writeCharacteristic(mac,"0000a003-0000-1000-8000-00805f9b34fb","0000a031-0000-1000-8000-00805f9b34fb",test);
                        int i=0;
                    }else{
                        int temp=0;
                        byte[] test=new byte[1];
                        test[0]=(byte)temp;
                        MyService.writeCharacteristic(mac,"0000a003-0000-1000-8000-00805f9b34fb","0000a031-0000-1000-8000-00805f9b34fb",test);
                        int i=0;
                    }
                    wait=false;
                    chengeBoolean(getAdapterPosition());

                    wait=true;

                }
            });
            if(getAdapterPosition()!=-1) {
                mSwitch.setChecked(onBoolean.get(getAdapterPosition()));
            }
        }
    }
    public void addAdapter(String data){
        onDataList.add(data);
        onBoolean.add(false);
    }
    public void deleteAdapter(String data){
        int i=0;
        for(String temp:onDataList){
            if(temp.equals(data)){
                onDataList.remove(i);
                onBoolean.remove(i);
                i++;
            }
        }
    }
    static void chengeBoolean(int position) {
        boolean setboolean = onBoolean.get(position);
        if (setboolean) {
            setboolean = false;
        } else {
            setboolean = true;
        }
        onBoolean.set(position, setboolean);

    }


}