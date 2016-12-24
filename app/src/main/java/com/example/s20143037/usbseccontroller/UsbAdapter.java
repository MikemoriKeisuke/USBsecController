package com.example.s20143037.usbseccontroller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import static android.support.v7.recyclerview.R.styleable.RecyclerView;

public class UsbAdapter extends RecyclerView.Adapter<UsbAdapter.ViewHolder> {
    private LayoutInflater onLayoutInflater;
     ArrayList<String> onDataList;
    static ArrayList<Boolean>onBoolean ;
    static boolean wait=true;

    int count=0;

    public UsbAdapter(Context context, ArrayList<String> dataList) {
        super();
        onLayoutInflater = LayoutInflater.from(context);
        onDataList = dataList;
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

        public ViewHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.UsbNameView);
            mSwitch=(Switch) v.findViewById(R.id.switch1);
            mSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bool=onBoolean.get(getAdapterPosition());

                    if(bool){
                        int i=0;
                    }else{
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