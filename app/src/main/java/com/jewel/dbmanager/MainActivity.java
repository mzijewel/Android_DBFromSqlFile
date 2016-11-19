package com.jewel.dbmanager;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.jewel.dbmanager.adapter.AdPerson;
import com.jewel.dbmanager.model.MPerson;

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

        DBManager.init(this, 3)
                .createTable(MPerson.class)
                .addNewColumn(MPerson.class, "age", "integer")
                .addNewColumn(MPerson.class, "des", "text")
                .build();

        DBManager.getInstance().addData(new MPerson("Mamta", "01989", 1252, 28, "wife"));
        init();
        prepareList();
        persons = DBManager.getInstance().getData(MPerson.class);
        Log.e("TE", "s:" + persons.size());

    }


    private void init() {
        edtName = (EditText) findViewById(R.id.edtName);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        btnSave = (Button) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(this);

        list = (ListView) findViewById(R.id.list);
        adapter = new AdPerson(this);

        persons = new ArrayList<>();
        list.setAdapter(adapter);

        adapter.setiUpdate(new AdPerson.IUpdate() {
            @Override
            public void onUpdate(int pos) {
                openDialog(pos);
            }
        });
    }

    private void openDialog(final int pos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dia);
        dialog.show();

        Button btnEdit = (Button) dialog.findViewById(R.id.btnEdit);
        Button btnDelete = (Button) dialog.findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog diaEdit = new Dialog(MainActivity.this);
                diaEdit.requestWindowFeature(Window.FEATURE_NO_TITLE);
                diaEdit.setContentView(R.layout.dia_edit);
                diaEdit.show();

                final EditText edtName = (EditText) diaEdit.findViewById(R.id.edtName);
                final EditText edtPhone = (EditText) diaEdit.findViewById(R.id.edtPhone);

                edtName.setText(persons.get(pos).getName());
                edtPhone.setText(persons.get(pos).getPhone());

                Button btnUpdate = (Button) diaEdit.findViewById(R.id.btnUpdate);
                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        persons.get(pos).setName(edtName.getText().toString());
                        persons.get(pos).setPhone(edtPhone.getText().toString());

                        DBManager.getInstance().addData(persons.get(pos));
                        diaEdit.dismiss();
                        dialog.dismiss();
                        prepareList();
                    }
                });
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBManager.getInstance().delete(MPerson.class, new Search("id", persons.get(pos).getId() + "", Search.EQUAL));
                dialog.dismiss();
                prepareList();
            }
        });

    }

    private void prepareList() {
        persons = DBManager.getInstance().getData(MPerson.class);
        adapter.addData(persons);

    }

    private void save() {
        MPerson person = new MPerson();
        person.setName(edtName.getText().toString());
        person.setPhone(edtPhone.getText().toString());
        person.setRoll(person.getName().length());
        DBManager.getInstance().addData(person);

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
