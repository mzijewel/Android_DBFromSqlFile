package com.jewel.dbmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jewel.dbmanager.R;
import com.jewel.dbmanager.model.MPerson;

import java.util.ArrayList;

/**
 * Created by Jewel on 10/30/2016.
 */

public class AdPerson extends BaseAdapter {
    private ArrayList<MPerson> persons;
    private IUpdate iUpdate;
    private Context context;

    public AdPerson(Context context) {
        persons = new ArrayList<>();
        this.context = context;
    }

    public void setiUpdate(IUpdate iUpdate) {
        this.iUpdate = iUpdate;
    }

    public void addData(ArrayList<MPerson> persons) {
        this.persons = persons;
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
        MPerson person = persons.get(position);
        MyViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_person, parent, false);
            viewHolder = new MyViewHolder();
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.tvPhone = (TextView) convertView.findViewById(R.id.tvPhone);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (MyViewHolder) convertView.getTag();
        viewHolder.tvName.setText(person.getName());
        viewHolder.tvPhone.setText(person.getPhone() + ":" + person.getAge() + ":" + person.getDes());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iUpdate != null) iUpdate.onUpdate(position);
            }
        });
        return convertView;
    }

    public interface IUpdate {
        void onUpdate(int pos);
    }

    class MyViewHolder {
        TextView tvName, tvPhone;
    }
}
