package com.example.loon.malhada;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by loon on 2016-11-15.
 */

public class PlugAdapter extends BaseAdapter {

    private ArrayList<PlugList> listViewItemList = new ArrayList<PlugList>() ;
    public PlugAdapter() {
    }
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.plugitem, parent, false);
        }
        TextView nameView = (TextView) convertView.findViewById(R.id.nameTv) ;
        TextView conView = (TextView) convertView.findViewById(R.id.conTv) ;
        ImageView statusView = (ImageView) convertView.findViewById(R.id.plug_status);
        PlugList listViewItem = listViewItemList.get(pos);
        nameView.setText(listViewItem.getName());

        if(listViewItem.getRegister() < 1) {
            nameView.setTextColor(Color.parseColor("#FFCACACA"));
        }
        else {
            nameView.setTextColor(Color.parseColor("#000000"));
        }

        if(listViewItem.getStatus() < 1) {
            statusView.setImageResource(R.drawable.power_off);
        }
        else {
            statusView.setImageResource(R.drawable.power_on);
        }


        conView.setText("Plug Serial : " + String.valueOf(listViewItem.getSerial()));
        return convertView;
    }
    @Override
    public long getItemId(int position) {
        return position ;
    }
    public  void clear(){
        int size = listViewItemList.size();
        while(size--!=0){
            listViewItemList.remove(0);
        }
    }
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }
    public void addItem(String name, String serial, int register, int status) {
        PlugList item = new PlugList();
        item.setName(name);
        item.setSerial(serial);
        item.setRegister(register);
        item.setStatus(status);
        listViewItemList.add(item);
        return;
    }
    public  void deleteItem(int position){
        listViewItemList.remove(position);
        return;
    }
}

