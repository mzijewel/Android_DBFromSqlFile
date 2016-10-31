package com.jewel.dbfromsql.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jewel.dbfromsql.R;
import com.jewel.dbfromsql.model.MPerson;
import com.jewel.dbfromsql.support.MyApp;

import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Created by Jewel on 10/30/2016.
 */

public class AdPerson extends BaseAdapter {
    private ArrayList<MPerson>persons;
    private IUpdate iUpdate;

    public AdPerson(){
        persons=new ArrayList<>();
    }
    public void setiUpdate(IUpdate iUpdate){
        this.iUpdate=iUpdate;
    }
    public void addData(ArrayList<MPerson>persons){
        this.persons=persons;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return persons.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MPerson person=persons.get(position);
        MyViewHolder viewHolder=null;
        if(convertView==null){
            convertView= LayoutInflater.from(MyApp.getContext()).inflate(R.layout.row_person,parent,false);
            viewHolder=new MyViewHolder();
            viewHolder.tvName= (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.tvPhone= (TextView) convertView.findViewById(R.id.tvPhone);
            convertView.setTag(viewHolder);
        }else
        viewHolder= (MyViewHolder) convertView.getTag();
        viewHolder.tvName.setText(person.getName());
        viewHolder.tvPhone.setText(person.getPhone());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(iUpdate!=null)iUpdate.onUpdate(position);
            }
        });
        return convertView;
    }

    class MyViewHolder {
        TextView tvName,tvPhone;
    }
    public interface IUpdate{
        void onUpdate(int pos);
    }
}
