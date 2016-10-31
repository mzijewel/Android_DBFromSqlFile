package com.jewel.dbfromsql.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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


    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";


    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "mydb";

    private static final String CREATE_TABLE_PERSON = DBQuery.init()
            .newTable(TABLE_PERSON)
            .addField(KEY_ID, DBQuery.INTEGER_PRI_AUTO)
            .addField(KEY_NAME, DBQuery.TEXT)
            .addField(KEY_PHONE, DBQuery.TEXT)
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

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create tables
        db.execSQL(CREATE_TABLE_PERSON);

        //load sql code from external file
        String queries = getStringFromFile(R.raw.default_db);
        for (String query : queries.split(";")) {
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //delete tables after upgrading
        db.execSQL(DBQuery.QUERY_DROP + TABLE_PERSON);

        //create tables after upgrading
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


    private boolean isExist(String table, String searchField, String value) {
        if (value.equals("") || Integer.valueOf(value) <= 0)
            return false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + table + " where " + searchField + "='" + value + "'", null);
            if (cursor != null && cursor.getCount() > 0)
                return true;
        } catch (Exception e) {

        } finally {
            if (cursor != null)
                cursor.close();

        }


        return false;
    }

    private String getStringValue(Cursor cursor, String key) {

        if (cursor.getColumnIndex(key) == -1)
            return "na";
        else
            return cursor.getString(cursor.getColumnIndex(key));
    }


    public long addData(String tableName, Object dataModelClass, String primaryKey) {
        long result = -1;
        String valueOfKey = "";
        try {
            Class myClass = dataModelClass.getClass();
            Field[] fields = myClass.getDeclaredFields();
            ContentValues contentValues = new ContentValues();

            for (Field field : fields) {
                //for getting access of private field


                String name = field.getName();
                field.setAccessible(true);
                Object value = field.get(dataModelClass);

                //ignore special field while refracting
                if (name.equalsIgnoreCase("serialVersionUID")
                        || name.equalsIgnoreCase("$change")
                        ) {

                } else {
                    if (!name.equals(primaryKey))
                        contentValues.put(name, value + "");
                    if (name.equalsIgnoreCase(primaryKey)) {
                        valueOfKey = value + "";
                    }

                }


            }
            if (!isExist(tableName, primaryKey, valueOfKey)) {
                result = db.insert(tableName, null, contentValues);
            } else {
                result = db.update(tableName, contentValues, primaryKey + "=?", new String[]{valueOfKey + ""});
            }


        } catch (Exception e) {
        } finally {

        }
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


    public <T> long addAllData(ArrayList<T> list, String table, String searchFiled, String valueOfField) {
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

            if (isExist(table, searchFiled, valueOfField)) {
                result = db.update(table, contentValues, KEY_ID + "=?", new String[]{id + ""});
            } else {
                result = db.insert(table, null, contentValues);

            }

        }

        return result;
    }

    public int delete(String table, String searchField, String value) {
        return db.delete(table, searchField + "=?", new String[]{value});
    }

}
