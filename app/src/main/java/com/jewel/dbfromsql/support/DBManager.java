package com.jewel.dbfromsql.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.jewel.dbfromsql.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Jewel on 11/9/2015.
 */
public class DBManager extends SQLiteOpenHelper {
    public static final String TABLE_PERSON = "tbl_person";
    public static final String TABLE_DETAILS = "tbl_details";


    public static final String KEY_ID = "id";
    public static final String KEY_FK_ID = "fk_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_OCCUPATION = "occupation";


    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "mydb";

    private static final String CREATE_TABLE_PERSON = DBQuery.init()
            .newTable(TABLE_PERSON)
            .addField(KEY_ID, DBQuery.INTEGER_PRI_AUTO)
            .addField(KEY_NAME, DBQuery.TEXT)
            .getTable();
    private static final String CREATE_TABLE_DETAILS = DBQuery.init()
            .newTable(TABLE_DETAILS)
            .addField(KEY_ID, DBQuery.INTEGER_PRI_AUTO)
            .addField(KEY_FK_ID,DBQuery.INTEGER)
            .addField(KEY_OCCUPATION, DBQuery.TEXT)
            .getTable();


    private static SQLiteDatabase db;
    private static DBManager instance;
    private Context context;

    private DBManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        db = this.getWritableDatabase();

    }

    public static DBManager getInstance() {
        if (instance == null)
            instance = new DBManager(MyApp.getContext());
        return instance;
    }

    public static String getQueryAll(String table) {
        return "select * from " + table;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PERSON);
        db.execSQL(CREATE_TABLE_DETAILS);


        String queries = getStringFromFile(R.raw.default_db);
        for (String query : queries.split(";")) {
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBQuery.QUERY_DROP + TABLE_PERSON);
        db.execSQL(DBQuery.QUERY_DROP + TABLE_DETAILS);


        onCreate(db);
    }

    private String getStringFromFile(int fileName) {
        InputStream inputStream = context.getResources().openRawResource(fileName);
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


    private boolean isExist(String table, int id) {

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + table + " where id='" + id + "'", null);
            if (cursor != null && cursor.getCount() > 0) {
                Log.e("DB","exist "+id);
                return true;
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null)
                cursor.close();

        }

        Log.e("DB","not exist "+id);
        return false;
    }

    private String getStringValue(Cursor cursor, String key) {

        if (cursor.getColumnIndex(key) == -1)
            return "na";
        else
            return cursor.getString(cursor.getColumnIndex(key));
    }

    private int getIntValue(Cursor cursor, String key) {
        if (cursor.getColumnIndex(key) == -1)
            return 0;
        else
            return cursor.getInt(cursor.getColumnIndex(key));
    }


    public long addData(String tableName, Object dataModelClass) {

        long result = -1;
        String id = "";
        try {
            Class myClass = dataModelClass.getClass();
            Field[] fields = myClass.getDeclaredFields();
            ContentValues contentValues = new ContentValues();

            for (Field field : fields) {
                //for getting access of private field
                field.setAccessible(true);
                Object value = field.get(dataModelClass);
                String name = field.getName();



                if (name != null && name.equals("id")){

                    id = value + "";
                    if(!id.equals("0")){
                        contentValues.put(name,id);
                    }

                }else
                    contentValues.put(name, value + "");

            }
            if (id != null)
                if (!isExist(tableName, Integer.valueOf(id))) {
                    Log.e("DB-inserted", id);
                    result = db.insert(tableName, null, contentValues);
                } else {
                    Log.e("DB-updated", id+":"+contentValues.get("name"));
                    db.update(tableName, contentValues, "id=?", new String[]{id + ""});
                }


        } catch (SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        } catch (IllegalAccessException ex) {
        } finally {

        }
        return result;
    }

    public <T> long addAllData(String tableName, ArrayList<T> dataModelClass) {
        long result = -1;
        Log.e("DB-addAllData", "add all");
        for (Object model : dataModelClass)
            result = addData(tableName, model);
        return result;
    }

    public <T> ArrayList<T> getData(String tableName, Object dataModelClass) {

        String sql = "select * from " + tableName;
        Cursor cursor = db.rawQuery(sql, null);
        JSONObject jsonObject = new JSONObject();
        final ArrayList<JSONObject> data = new ArrayList<JSONObject>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                jsonObject = new JSONObject();
                try {
                    Class myClass = dataModelClass.getClass();
                    Field[] fields = myClass.getDeclaredFields();

                    for (Field field : fields) {
                        //for getting access of private field
                        field.setAccessible(true);
                        String name = field.getName();

                        jsonObject.put(name, getStringValue(cursor, name));

                    }
                    data.add(jsonObject);

                } catch (SecurityException ex) {
                } catch (IllegalArgumentException ex) {
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        Gson gson = new Gson();
        ArrayList<T> output = new ArrayList<T>();
        for (int i = 0; i < data.size(); i++) {
            dataModelClass = gson.fromJson(data.get(i).toString(), dataModelClass.getClass());
            output.add((T) dataModelClass);
        }


        return output;
    }


    public <T> long addData(ArrayList<T> list, String table) {
        long result = -1;


        for (T data : list) {
            ContentValues contentValues = new ContentValues();
            int id = 0;
            try {
                Class myClass = data.getClass();
                Field[] fields = myClass.getDeclaredFields();
                for (Field field : fields) {
                    //for getting access of private field
                    field.setAccessible(true);
                    Object value = field.get(data);
                    String name = field.getName();

                    contentValues.put(name, value != null ? value.toString() : "");
                    if (name.equalsIgnoreCase("id"))
                        id = Integer.valueOf(value.toString());

                }
            } catch (SecurityException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (IllegalAccessException ex) {
            }

            if (isExist(table, id)) {
                result = db.update(table, contentValues, KEY_ID + "=?", new String[]{id + ""});
            } else {
                result = db.insert(table, null, contentValues);

            }

        }

        return result;
    }


    public <T> ArrayList<T> getList(String sql, Object myInstance) {

        ArrayList<T> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        Gson gson = new Gson();
        if (cursor != null && cursor.moveToFirst()) {
            JSONObject jsonObject = new JSONObject();
            do {

                try {
                    Class myClass = myInstance.getClass();
                    Field[] fields = myClass.getDeclaredFields();
                    for (Field field : fields) {
                        //for getting access of private field
                        field.setAccessible(true);
                        String name = field.getName();
                        jsonObject.put(name, getStringValue(cursor, name));

                    }
                } catch (SecurityException ex) {
                } catch (IllegalArgumentException ex) {
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                myInstance = gson.fromJson(jsonObject.toString(), myInstance.getClass());
                list.add((T) myInstance);
            } while (cursor.moveToNext());
        }

        return list;
    }


}
