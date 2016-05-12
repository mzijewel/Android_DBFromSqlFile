package com.jewel.dbfromsql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jewel.dbfromsql.model.MPerson;
import com.jewel.dbfromsql.support.DBManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* DBManager db=DBManager.getInstance();
        ArrayList<MPerson> data = db.getData(DBManager.TABLE_PERSON, new MPerson());
        ((TextView) findViewById(R.id.tv)).setText(data.get(0).getName());*/


    }


}
