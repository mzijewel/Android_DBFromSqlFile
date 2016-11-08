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
    private static Context context;

    private static final int DB_VERSION = 1;
    private static String DB_NAME = "MyDB";
    private static ArrayList<String> tableQueries = new ArrayList<>();
    private static ArrayList<String> tables = new ArrayList<>();
    private static SQLiteDatabase db;
    private static DBManager instance;



    public static void init(Context mContext){
        context=mContext;
        DB_NAME = context.getPackageName().substring(context.getPackageName().lastIndexOf("."));
    }
    private DBManager() {
        super(context, DB_NAME, null, DB_VERSION);
        db = this.getWritableDatabase();

    }

    public static DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();
        return instance;
    }

    public static void createTable(Class classOfT) {
        String dbField = "", table = classOfT.getSimpleName();
        String sql = "create table if not exists " + table + "(";

        Field[] fields = classOfT.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String type = field.getGenericType().toString();

            //ignore special field while refracting
            if (name.equalsIgnoreCase("serialVersionUID")
                    || name.equalsIgnoreCase("$change")
                    ) {

            } else {
                dbField += name + " " + getType(name, type) + ",";
            }

        }
        if (!tableQueries.contains(sql))
            tableQueries.add(sql + " " + dbField.substring(0, dbField.length() - 1) + ")");
        if (!tables.contains(table))
            tables.add(table);
    }

    private static String getType(String name, String type) {
        if (name.equals("id")) return "integer primary key autoincrement";
        if (type.equalsIgnoreCase("int")) {
            return "integer";
        }
        return "text";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //load sql code from external file
//        String queries = getStringFromFile(R.raw.default_db);
//        for (String query : queries.split(";")) {
//            db.execSQL(query);
//        }

        //create tables
        for (String query : tableQueries) {
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //upgrade table
//        db.execSQL("ALTER TABLE tableName ADD COLUMN roll integer");

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

    public long addData(Object dataModelClass, String primaryKey) {
        long result = -1;
        String valueOfKey = "", tableName = "";
        tableName = dataModelClass.getClass().getSimpleName();
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
                    if (name.equals(primaryKey)&& Integer.parseInt(value+"")==0){
                    }
                    else {
                        contentValues.put(name, value + "");
                    }
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

    public <T> ArrayList<T> getData(Class classOfT) {
        String sql = "select * from " + classOfT.getSimpleName();
        Cursor cursor = db.rawQuery(sql, null);
        JSONObject jsonObject = new JSONObject();
        final ArrayList<JSONObject> data = new ArrayList<JSONObject>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                jsonObject = new JSONObject();
                try {
                    Field[] fields = classOfT.getDeclaredFields();

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
            output.add((T) gson.fromJson(data.get(i).toString(), classOfT));
        }


        return output;
    }

    public <T> ArrayList<T> getData(Class classOfT, Search... searches) {
        String searchQ = "";
        for (int i = 0; i < searches.length; i++) {
                searchQ += searches[i].getField() + searches[i].getOperator() + "'" + searches[i].getValue() + "'";
            if (searches.length>1 && i!=searches.length-1)
                searchQ+=" OR ";
        }
        String sql = "select * from " + classOfT.getSimpleName() + " where " + searchQ;
        Cursor cursor = db.rawQuery(sql, null);
        JSONObject jsonObject = new JSONObject();
        final ArrayList<JSONObject> data = new ArrayList<JSONObject>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                jsonObject = new JSONObject();
                try {
                    Field[] fields = classOfT.getDeclaredFields();

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
            output.add((T) gson.fromJson(data.get(i).toString(), classOfT));
        }


        return output;
    }

    public <T> ArrayList<T> getData(Class classOfT, String sql) {

        Cursor cursor = db.rawQuery(sql, null);
        JSONObject jsonObject = new JSONObject();
        final ArrayList<JSONObject> data = new ArrayList<JSONObject>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                jsonObject = new JSONObject();
                try {
                    Field[] fields = classOfT.getDeclaredFields();

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
            output.add((T) gson.fromJson(data.get(i).toString(), classOfT));
        }


        return output;
    }

    public <T> void addData(ArrayList<T> list, String primaryKey) {
        for (T t : list) {
            addData(t, primaryKey);
        }

    }

    public int delete(Class modelClass, String searchField, String value) {
        return db.delete(modelClass.getSimpleName(), searchField + "=?", new String[]{value});
    }


}

