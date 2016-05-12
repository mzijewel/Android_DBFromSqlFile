package com.jewel.dbfromsql;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.jewel.dbfromsql.model.MPerson;
import com.jewel.dbfromsql.support.DBManager;

import java.util.ArrayList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class DBTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public DBTest() {
        super(MainActivity.class);
    }
    public void testDB(){

        DBManager db=DBManager.getInstance();
        MPerson person=new MPerson();
        person.setId(1);
        person.setName("Test 1");
        db.addData(DBManager.TABLE_PERSON, person);

        ArrayList<MPerson>persons=db.getData(DBManager.TABLE_PERSON,new MPerson());
        for(int i=0;i<persons.size();i++)
        Log.e("UNIT_TEST","ID : "+persons.get(i).getId()+" NAME : "+persons.get(i).getName());

    }
}