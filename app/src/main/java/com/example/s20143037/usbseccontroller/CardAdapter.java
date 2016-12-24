package com.example.s20143037.usbseccontroller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by kento on 2016/12/22.
 */

public class CardAdapter extends RecyclerView.Adapter {
    private Context ctx;
    private List<String> card;
    public CardAdapter(Context context,List<String> cardList){
        ctx=context;
        card=cardList;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
    public void addList(String title){
        card.add(title);
    }
}
