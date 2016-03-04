package com.jewel.dbfromsql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Jewel on 3/4/2016.
 */
public class DB extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private Context context;
    public DB(Context context) {
        super(context, "testDb", null, 1);
        this.context=context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String queries=getStringFromFile(R.raw.default_db);
        for(String query:queries.split(";")){
            db.execSQL(query);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * getting data as String from resource file
     * file have to be in res/ folder
     */
    private String getStringFromFile(int fileName){
        InputStream inputStream=context.getResources().openRawResource(fileName);
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }

    public long addData(String name){
        if(db==null)
            db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("name", name);

        return db.insert("test",null,values);
    }

    public String getData(){
        String out="";
        if(db==null)
            db=this.getWritableDatabase();
        Cursor c=db.rawQuery("select * from test", null);
        if(c!=null && c.moveToFirst()){
            do{
                out+=c.getString(1)+"\n";
            }while (c.moveToNext());
        }
        return out;
    }

    public void dbClose(){
        if(db!=null && db.isOpen())
            db.close();
    }
}
