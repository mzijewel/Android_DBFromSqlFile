package com.jewel.dbmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;

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
public class DBManager {
    private static int DB_VERSION = 1;
    private static Context context;
    private static String DB_NAME = "MyDB";
    private static ArrayList<String> tableQueries = new ArrayList<>();
    private static ArrayList<String> alterTables = new ArrayList<>();
    private static MyDB myDB;
    private static DBManager instance;

    private ArrayList<String> tables;
    private ArrayList<String> fields;
    private ArrayList<String> types;


    private DBManager() {

    }

    public static DBManager init(Context mContext) {
        context = mContext;
        DB_NAME = context.getPackageName().substring(context.getPackageName().lastIndexOf("."));
        return getInstance();
    }

    public static DBManager init(Context mContext, int version) {
        context = mContext;
        DB_NAME = context.getPackageName().substring(context.getPackageName().lastIndexOf("."));
        DB_VERSION = version;
        return getInstance();
    }

    public static DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();
        return instance;
    }

    private static String getType(String type, DB.KEY key) {
        if (key != null && key == DB.KEY.PRIMARY) return "integer primary key autoincrement";
        if (type.equalsIgnoreCase("int")) {
            return "integer";
        }
        return "text";
    }

    public void build() {
        myDB = new MyDB(context);
    }

    public DBManager createTable(Class classOfT) {
        String dbField = "", table = classOfT.getSimpleName();
        String sql = "create table if not exists " + table + "(";

        Field[] fields = classOfT.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            DB.KEY dbKey = null;
            String name = field.getName();
            String type = field.getGenericType().toString();
            DB dbAnn = field.getAnnotation(DB.class);
            if (dbAnn != null)
                dbKey = dbAnn.key();
            //ignore special field while refracting
            if (name.equalsIgnoreCase("serialVersionUID")
                    || name.equalsIgnoreCase("$change")
                    ) {

            } else {
                dbField += name + " " + getType(type, dbKey) + ",";
            }

        }
        if (tables == null) tables = new ArrayList<>();
        if (tableQueries == null) tableQueries = new ArrayList<>();
        if (!tableQueries.contains(sql))
            tableQueries.add(sql + " " + dbField.substring(0, dbField.length() - 1) + ")");
        if (!tables.contains(table))
            tables.add(table);

        return instance;
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
            cursor = myDB.db.rawQuery("select * from " + table + " where " + searchField + "='" + value + "'", null);
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

    public long addData(Object dataModelClass) {
        String primaryKey = "id";
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
                DB dbKey = field.getAnnotation(DB.class);
                if (dbKey != null)
                    primaryKey = name;


                //ignore special field while refracting
                if (name.equalsIgnoreCase("serialVersionUID")
                        || name.equalsIgnoreCase("$change")
                        ) {

                } else {
                    if (name.equals(primaryKey) && Integer.parseInt(value + "") == 0) {
                    } else {
                        contentValues.put(name, value + "");
                    }
                    if (name.equalsIgnoreCase(primaryKey)) {
                        valueOfKey = value + "";
                    }

                }


            }
            if (!isExist(tableName, primaryKey, valueOfKey)) {
                result = myDB.db.insert(tableName, null, contentValues);
            } else {
                result = myDB.db.update(tableName, contentValues, primaryKey + "=?", new String[]{valueOfKey + ""});
            }


        } catch (Exception e) {
        } finally {

        }
        return result;
    }

    public <T> ArrayList<T> getData(Class classOfT) {
        String sql = "select * from " + classOfT.getSimpleName();
        Cursor cursor = myDB.db.rawQuery(sql, null);
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
            if (i != searches.length - 1) {
                if (searches[i].getNextOperator() != null && !searches[i].getNextOperator().equals(""))
                    searchQ += searches[i].getNextOperator();
                else
                    searchQ += Search.AND;
            }

        }
        String sql = "select * from " + classOfT.getSimpleName() + " where " + searchQ;
        Log.e("DB", sql);
        Cursor cursor = myDB.db.rawQuery(sql, null);
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

        Cursor cursor = myDB.db.rawQuery(sql, null);
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

    public <T> void addData(ArrayList<T> list) {
        for (T t : list) {
            addData(t);
        }

    }

    public int delete(Class modelClass, Search... searches) {
        String fields = "";
        String[] values = new String[searches.length];
        for (int i = 0; i < searches.length; i++) {
            Search search = searches[i];
            fields += search.getField() + search.getOperator() + "?";
//            if (search.getNextOperator() != null && !search.getNextOperator().equals(""))
//                fields += search.getNextOperator();

            if (i != searches.length - 1) {
                if (searches[i].getNextOperator() != null && !searches[i].getNextOperator().equals(""))
                    fields += search.getNextOperator();
                else
                    fields += Search.AND;
            }

            values[i] = search.getValue();
        }
        return myDB.db.delete(modelClass.getSimpleName(), fields, values);
    }

    public void rawQuery(String sql) {
        myDB.db.rawQuery(sql, null);
    }

    public DBManager addNewColumn(Class table, String field, String type) {
        if (alterTables == null) alterTables = new ArrayList<>();
        if (fields == null) fields = new ArrayList<>();
        if (types == null) types = new ArrayList<>();

        alterTables.add(table.getSimpleName());
        fields.add(field);
        types.add(type);

        return instance;
    }

    public class MyDB extends SQLiteOpenHelper {
        public SQLiteDatabase db;

        public MyDB(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            db = this.getWritableDatabase();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            //load sql code from external file
//        String queries = getStringFromFile(R.raw.default_db);
//        for (String query : queries.split(";")) {
//            myDB.execSQL(query);
//        }

            //create tables
            for (String query : tableQueries) {
                db.execSQL(query);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //upgrade table
            try {
                if (alterTables != null && alterTables.size() > 0)
                    for (int i = 0; i < alterTables.size(); i++)
                        db.execSQL("ALTER TABLE " + alterTables.get(i) + " ADD COLUMN " + fields.get(i) + " " + types.get(i));

            } catch (Exception e) {

            }

        }
    }


}

