package com.example.s20143037.usbseccontroller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class UsbAdapter extends RecyclerView.Adapter<UsbAdapter.ViewHolder> {
    private LayoutInflater onLayoutInflater;
    private ArrayList<String> onDataList;

    public UsbAdapter(Context context, ArrayList<String> dataList) {
        super();
        onLayoutInflater = LayoutInflater.from(context);
        onDataList = dataList;
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
        holder.text.setText(data);
    }

    // ViewHolder内でwidgetを割り当てる
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public ViewHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.UsbNameView);
        }
    }
}