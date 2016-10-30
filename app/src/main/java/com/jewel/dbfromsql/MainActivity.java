package com.jewel.dbfromsql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.jewel.dbfromsql.adapter.AdPerson;
import com.jewel.dbfromsql.model.MPerson;
import com.jewel.dbfromsql.support.DBManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtName, edtPhone;
    private Button btnSave;
    private ArrayList<MPerson> persons;
    private ListView list;
    private AdPerson adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        prepareList();



    }

    private void init() {
        edtName = (EditText) findViewById(R.id.edtName);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        btnSave = (Button) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(this);

        list = (ListView) findViewById(R.id.list);
        adapter = new AdPerson();

        persons = new ArrayList<>();
        list.setAdapter(adapter);


    }

    private void prepareList() {
        persons = DBManager.getInstance().getData(DBManager.TABLE_PERSON, new MPerson());
        adapter.addData(persons);

    }

    private void save() {
        MPerson person = new MPerson();
        person.setName(edtName.getText().toString());
        person.setPhone(edtPhone.getText().toString());
        DBManager.getInstance().addData(DBManager.TABLE_PERSON, person,"id");

        edtName.setText("");
        edtPhone.setText("");

        prepareList();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                save();
                break;
        }
    }
}
