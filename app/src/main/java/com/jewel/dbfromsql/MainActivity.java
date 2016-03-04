package com.jewel.dbfromsql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DB db = new DB(this);
        String data = db.getData();
        ((TextView) findViewById(R.id.tv)).setText(data);
        db.dbClose();

    }


}
