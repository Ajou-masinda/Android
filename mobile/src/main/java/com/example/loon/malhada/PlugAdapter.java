package com.example.loon.malhada;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
        PlugList listViewItem = listViewItemList.get(pos);
        nameView.setText(listViewItem.getName());
        conView.setText(String.valueOf(listViewItem.getCondition()));
        return convertView;
    }
    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }
    public void addItem(String name, int con) {
        PlugList item = new PlugList();
        item.setName(name);
        item.setCondition(con);
        listViewItemList.add(item);
        return;
    }
    public  void deleteItem(int position){
        listViewItemList.remove(position);
        return;
    }
}

